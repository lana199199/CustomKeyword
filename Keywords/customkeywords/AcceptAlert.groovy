package customkeywords

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.text.MessageFormat
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testcase.TestCaseFactory
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testdata.TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.webui.common.WebUiCommonHelper
import com.kms.katalon.core.webui.constants.StringConstants
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.mobile.helper.MobileElementCommonHelper
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.exception.WebElementNotFoundException

class KatalonAlert {
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def handleNextAlert() {
		handleNextAlert(true);
	}
		​
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def handleNextAlert(Object valueToReturn) {
		WebUI.executeJavaScript("""
			function waitfunction() {
			    var a = 5000 + new Date().getTime();
			    while (new Date() < a) {}
			}
​
			function notifyAlert() {
				if (window._katNotifyAlert) {
					window._katNotifyAlert(true);
					window._katNotifyAlert = null;
				} else {
					if (window._katNotifyCount == 1) {
						window._katNotifyCount = null;
						return;
					}
					if (!window._katNotifyCount) {
						window._katNotifyCount = 3 + 1;
					} else {
						window._katNotifyCount -= 1;
					}
					setTimeout(notifyAlert, 100);
				}
			}

			let _katalonAlertHandler = (...args) => {
				window._katAlerts = window._katAlerts || [];
				window._katAlerts.push(args[0]);
				console.log('Alert', arguments[0], args);
//				var value = waitfunction();
//				return value;
				notifyAlert();
				return arguments[0];
			}
			window._katConfirm = window._katConfirm || window.confirm;
			window._katAlert = window._katAlert || window.alert;
			window._katPrompt = window._katPrompt || window.prompt;
			window.alert = window.confirm = window.prompt = _katalonAlertHandler;
		""", [valueToReturn]);
	}
	​
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def void acceptNextAlert() {
		handleNextAlert(true);
	}
	​
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def void acceptNextAlertIf() {
		handleNextAlert(true);
	}
	​
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def void denyNextAlert() {
		handleNextAlert(false);
	}
	​
	/**
	 * This keyword must be called before the alert trigger
	 */
	@Keyword
	def void acceptNextPrompt(String value) {
		handleNextAlert(value);
	}
	​
	/**
	 * One of these keywords must be called before the alert trigger:
	 * 		handleNextAlert, acceptNextAlert, denyNextAlert, acceptNextPrompt
	 */
	@Keyword
	def String getLastAlertMessage() {
		return (String) WebUI.executeJavaScript("""
			window._katAlerts = window._katAlerts || [];
			return window._katAlerts[window._katAlerts.length - 1];
		""", []);
	}
	​
	/**
	 * One of these keywords must be called before the alert trigger:
	 * 		handleNextAlert, acceptNextAlert, denyNextAlert, acceptNextPrompt
	 */
	@Keyword
	def boolean waitForAlert(int timeout = 0) {
		timeout = WebUiCommonHelper.checkTimeout(timeout);
		return _waitForAlert(timeout);
	}
	​
	def boolean _waitForAlert(int timeout = 0) {
		WebDriver driver = DriverFactory.getWebDriver();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		boolean isAlertPresent = (boolean) js.executeAsyncScript("""
			var callback = arguments[arguments.length - 1].bind(this);
			window._katNotifyAlert = callback;
			setTimeout(function() {
				callback(false);
			}, arguments[0]);
		""", [timeout * 1000]);
		if (isAlertPresent) {
			KeywordUtil.markPassed(MessageFormat.format(StringConstants.KW_LOG_PASSED_ALERT_IS_PRESENT_AFTER_X_SEC, timeout))
		} else {
			KeywordUtil.markWarning(MessageFormat.format(StringConstants.KW_MSG_NO_ALERT_FOUND_AFTER_X_SEC, timeout))
		}
		return isAlertPresent;
	}
	​
	/**
	 * One of these keywords must be called before the alert trigger:
	 * 		handleNextAlert, acceptNextAlert, denyNextAlert, acceptNextPrompt
	 */
	@Keyword
	def boolean verifyAlertPresent(int timeout = 0, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE) {
		timeout = WebUiCommonHelper.checkTimeout(timeout);
		boolean isAlertPresent = _waitForAlert(timeout);
		if (isAlertPresent) {
			KeywordUtil.markPassed(MessageFormat.format(StringConstants.KW_LOG_PASSED_ALERT_IS_PRESENT_AFTER_X_SEC, timeout));
			return true;
		} else {
			String errorMessage = MessageFormat.format(StringConstants.KW_MSG_NO_ALERT_FOUND_AFTER_X_SEC, timeout);
			if (flowControl == FailureHandling.STOP_ON_FAILURE) {
				KeywordUtil.markFailedAndStop(errorMessage);
			} else {
				KeywordUtil.markWarning(errorMessage)
			}
			return false;
		}
	}
	​
	/**
	 * One of these keywords must be called before the alert trigger:
	 * 		handleNextAlert, acceptNextAlert, denyNextAlert, acceptNextPrompt
	 */
	@Keyword
	def boolean verifyAlertNotPresent(int timeout = 0, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE) {
		timeout = WebUiCommonHelper.checkTimeout(timeout);
		boolean isAlertPresent = _waitForAlert(timeout);
		if (isAlertPresent) {
			String errorMessage = MessageFormat.format(StringConstants.KW_LOG_PASSED_ALERT_IS_PRESENT_AFTER_X_SEC, timeout);
			if (flowControl == FailureHandling.STOP_ON_FAILURE) {
				KeywordUtil.markFailedAndStop(errorMessage);
			} else {
				KeywordUtil.markWarning(errorMessage)
			}
			return false;
		} else {
			KeywordUtil.markPassed(MessageFormat.format(StringConstants.KW_MSG_NO_ALERT_FOUND_AFTER_X_SEC, timeout));
			return true;
		}
	}
}
