package customkeywords;
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.SelectorMethod
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows

import internal.GlobalVariable
import java.util.stream.Collectors

import org.apache.commons.lang3.StringUtils
import org.openqa.selenium.WebElement

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.common.WebUiCommonHelper
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.keyword.internal.WebUIAbstractKeyword

public class SetTextHelper {

	public enum SetTextMethod {
		BUILT_IN_KEYWORD("Katalon Built-in Keyword"), JAVASCRIPT("Javascript"), JQUERY("JQuery");

		public String name;

		SetTextMethod(String name) {
			this.name = name;
		}
	}

	@Keyword
	def setCellText(int row, int column, String text, int timeout = 0) {
		TestObject cellObject = new TestObject();
		cellObject.selectorMethod = SelectorMethod.CSS;
		String selector = "[data-cid=\"${row}\$${column}\"]";
		cellObject.getSelectorCollection().put(SelectorMethod.CSS, selector.toString());
		setCellText(cellObject, text, timeout);
	}

	@Keyword
	def setCellText(TestObject cellObject, String text, int timeout = 0) {
		WebUI.doubleClick(cellObject);

		TestObject cellEditorObject = new TestObject();
		cellEditorObject.selectorMethod = SelectorMethod.CSS;
		cellEditorObject.getSelectorCollection().put(SelectorMethod.CSS, ".ss-editor-inner .ss-cell");

		def cellEditor = findElement(cellEditorObject, timeout);

		WebUI.executeJavaScript("""
			var element = arguments[0];
			var text = arguments[1];
			element.textContent = text;
			element.dispatchEvent(new Event('input', { bubbles: true, cancelable: true, composed: true }));
			element.dispatchEvent(new Event('change', { bubbles: true, cancelable: true, composed: true }));
			element.blur();
		""", [cellEditor, text]);
	}

	@Keyword
	WebElement findElement(TestObject testObject, int timeout = 0) {
		return WebUIAbstractKeyword.findWebElement(testObject, timeout);
	}

	WebElement findElementWithLog(TestObject testObject, int timeout = 0) {
		def element = WebUIAbstractKeyword.findWebElement(testObject, timeout);
		KeywordUtil.logInfo("Element can be found by the provided test object");
		return element;
	}

	def logSetTextMethodResult(String methodName) {
		KeywordUtil.logInfo("Element text has been set by using \"${methodName}\".");
		KeywordUtil.logInfo("In case this method didn't work, try to analyze with 'verifyElementSetTextable' keyword");
	}

	def handleKeywordResult(String message, FailureHandling flowControl) {
		if (flowControl == FailureHandling.OPTIONAL) {
			KeywordUtil.markWarning(message);
		}
		if (flowControl == FailureHandling.CONTINUE_ON_FAILURE) {
			KeywordUtil.markFailed(message);
		}
		if (flowControl == FailureHandling.STOP_ON_FAILURE) {
			KeywordUtil.markFailedAndStop(message);
		}
	}

	@Keyword
	def clearText(WebElement element) {
		element.clear();
		WebUI.executeJavaScript("""
			var element = arguments[0];
			element.value = '';
			element.dispatchEvent(new Event('input', { bubbles: true, cancelable: true, composed: true }));
			element.dispatchEvent(new Event('change', { bubbles: true, cancelable: true, composed: true }));
			if (element.form && element.form.querySelectorAll('input').length <= 1) {
				element.form.reset();
			}
		""", [element]);
	}

	@Keyword
	def unfocusElement() {
		WebUI.executeJavaScript("""
			var element = document.activeElement;
			element.blur();
			element.dispatchEvent(new Event('blur', { bubbles: true, cancelable: true, composed: true }));
			document.body.focus();
		""", []);
	}

	// ---

	@Keyword
	def setText(TestObject testObject, String text, SetTextMethod method, int timeout = 0, FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling()) {
		if (method == SetTextMethod.BUILT_IN_KEYWORD) {
			return this.setTextWithBuiltInKeyword(testObject, text, timeout, flowControl);
		}
		if (method == SetTextMethod.JAVASCRIPT) {
			return this.setTextWithJavascript(testObject, text, timeout, flowControl);
		}
		if (method == SetTextMethod.JQUERY) {
			return this.setTextWithJQuery(testObject, text, timeout, flowControl);
		}
	}

	@Keyword
	def setTextWithBuiltInKeyword(TestObject testObject, String text, int timeout = 0, FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling()) {
		def element = this.findElementWithLog(testObject, timeout);

		this.setTextWithBuiltInKeyword(element, text);

		this.logSetTextMethodResult("Katalon built-in keyword");
	}

	@Keyword
	def setTextWithJavascript(TestObject testObject, String text, int timeout = 0, FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling()) {
		def element = this.findElementWithLog(testObject, timeout);

		boolean isSwitchIntoFrame = WebUiCommonHelper.switchToParentFrame(testObject);

		this.setTextWithJavascript(element, text);

		if (isSwitchIntoFrame) {
			WebUiCommonHelper.switchToDefaultContent()
		}

		this.logSetTextMethodResult("Javascript");

		if (!this.verifyValueStable(element, text)) {
			this.handleKeywordResult("Value might have been set by using Javascript but it's not stable", flowControl);
		}
	}

	@Keyword
	def setTextWithJQuery(TestObject testObject, String text, int timeout = 0, FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling()) {
		def element = this.findElementWithLog(testObject, timeout);

		boolean isSwitchIntoFrame = WebUiCommonHelper.switchToParentFrame(testObject);

		this.setTextWithJQuery(element, text);

		if (isSwitchIntoFrame) {
			WebUiCommonHelper.switchToDefaultContent()
		}

		this.logSetTextMethodResult("JQuery");
		if (!this.verifyValueStable(element, text)) {
			this.handleKeywordResult("Value might have been set by using JQuery but it's not stable", flowControl);
		}
	}

	// ---

	@Keyword
	def setText(WebElement element, String text, SetTextMethod method) {
		if (method == SetTextMethod.BUILT_IN_KEYWORD) {
			return this.setTextWithBuiltInKeyword(element, text);
		}
		if (method == SetTextMethod.JAVASCRIPT) {
			return this.setTextWithJavascript(element, text);
		}
		if (method == SetTextMethod.JQUERY) {
			return this.setTextWithJQuery(element, text);
		}
	}

	@Keyword
	def setTextWithBuiltInKeyword(WebElement element, String text) {
		element.click(); // Some input has some special handlers on focus
		element.sendKeys(text);
		this.unfocusElement();
	}

	@Keyword
	def setTextWithJavascript(WebElement element, String text) {
		element.click(); // Some input has some special handlers on focus
		WebUI.executeJavaScript("""
			var element = arguments[0];
			var text = arguments[1];
			element.value = text;
			element.dispatchEvent(new Event('input', { bubbles: true, cancelable: true, composed: true }));
			element.dispatchEvent(new Event('change', { bubbles: true, cancelable: true, composed: true }));
		""", [element, text]);
	}

	@Keyword
	def setTextWithJQuery(WebElement element, String text) {
		element.click(); // Some input has some special handlers on focus
		this.installJQuery();
		WebUI.executeJavaScript("""
			if (!window.jQuery) {
				return;
			}
			var element = arguments[0];
			var text = arguments[1];
			jQuery(element).val(text);
			jQuery(element).trigger('input');
			jQuery(element).trigger('change');
		""", [element, text]);
	}

	def isJQueryAvailable() {
		return WebUI.executeJavaScript("""
			return !!window.jQuery;
		""", []);
	}

	@Keyword
	def installJQuery() {
		if (!this.isJQueryAvailable()) {
			WebUI.executeJavaScript(JQueryMin.src, null);
		}
	}

	/**
	 * @param analyzeAllMethods Set to `true` to check for all methods. Default to `false` - Will stop after we found a method that works.
	 */
	@Keyword
	def verifyElementSetTextable(TestObject testObject, String text, boolean analyzeAllMethods = false, int timeout = 0) {
		def element = this.findElement(testObject, timeout);
		List<SetTextMethod> succeededMethods = [];

		// Make sure we're in the right frame
		boolean isSwitchIntoFrame = WebUiCommonHelper.switchToParentFrame(testObject);

		// Try with the built-in set text keyword
		def builtInSetTextSignal = this.injectVerifier(element);
		this.setTextWithBuiltInKeyword(element, text);
		if (this.checkVerifier(element, text, builtInSetTextSignal)) {
			String message = 'Element text can be set by using "Katalon built-in keyword"'
			KeywordUtil.logInfo(message);
			if (!analyzeAllMethods) {
				KeywordUtil.markPassed(message);
				if (isSwitchIntoFrame) {
					WebUiCommonHelper.switchToDefaultContent()
				}
				return true;
			} else {
				succeededMethods.add(SetTextMethod.BUILT_IN_KEYWORD);
			}
		} else {
			String message = 'Element text cannot be set by using "Katalon built-in keyword"';
			KeywordUtil.logInfo(message);
		}

		// Try with normal JS
		if (analyzeAllMethods) {
			clearText(element);
		}
		def jsSetTextSignal = this.injectVerifier(element);
		this.setTextWithJavascript(element, text);
		if (this.checkVerifier(element, text, jsSetTextSignal)) {
			String message = 'Element text can be set by using "Javascript"';
			KeywordUtil.logInfo(message);
			if (!analyzeAllMethods) {
				KeywordUtil.markPassed(message);
				if (isSwitchIntoFrame) {
					WebUiCommonHelper.switchToDefaultContent()
				}
				return true;
			} else {
				succeededMethods.add(SetTextMethod.JAVASCRIPT);
			}
		} else {
			String message = 'Element text cannot be set by using "Javascript"';
			KeywordUtil.logInfo(message);
		}

		// Try with JQuery
		if (analyzeAllMethods) {
			clearText(element);
		}
		def jquerySetTextSignal = this.injectVerifier(element);
		this.setTextWithJQuery(element, text);
		if (this.checkVerifier(element, text, jquerySetTextSignal)) {
			String message = 'Element text can be set by using "JQuery"';
			KeywordUtil.logInfo(message);
			if (!analyzeAllMethods) {
				KeywordUtil.markPassed(message);
				if (isSwitchIntoFrame) {
					WebUiCommonHelper.switchToDefaultContent()
				}
				return true;
			} else {
				succeededMethods.add(SetTextMethod.JQUERY);
			}
		} else {
			String message = 'Element text cannot be set by using "JQuery"';
			KeywordUtil.logInfo(message);
		}

		// Conclusion
		boolean setTextable = succeededMethods.size() > 0;
		if (setTextable) {
			def succeededMethodNames = succeededMethods.collect({ it.name })
			KeywordUtil.markPassed("Element text can be set by using: \"${StringUtils.join(succeededMethodNames, '"')}\"");
			this.setText(element, text, succeededMethods.get(0));
		} else {
			String message = 'Cannot find any way to set text to the target element. Maybe your app has some special process for this element. Please check with your dev team to see if they can update the input to follow the Web standard.';
			KeywordUtil.logInfo(message);
			KeywordUtil.markWarning(message);
		}

		if (isSwitchIntoFrame) {
			WebUiCommonHelper.switchToDefaultContent()
		}

		return setTextable;
	}

	String injectVerifier(element) {
		return WebUI.executeJavaScript("""
			var signal = Math.random();
			var element = arguments[0];

			if (!window.__katalonSignals) {
				window.__katalonSignals = {}
			}

			function inputListener(event) {
				window.__katalonSignals[signal + '_input'] = true;
				element.removeEventListener('input', inputListener, true);
			};

			function changeListener(event) {
				window.__katalonSignals[signal + '_change'] = true;
				element.removeEventListener('change', changeListener, true);
			};

			function inputCaptureListener(event) {
				window.__katalonSignals[signal + '_inputCapture'] = true;
				element.removeEventListener('input', inputCaptureListener, true);
			};

			function changeCaptureListener(event) {
				window.__katalonSignals[signal + '_changeCapture'] = true;
				element.removeEventListener('change', changeCaptureListener, true);
			};

			element.addEventListener('input', inputListener, false);
			element.addEventListener('change', changeListener, false);
			element.addEventListener('input', inputCaptureListener, true);
			element.addEventListener('change', changeCaptureListener, true);

			if (window.__removeKatalonListeners) {
				window.__removeKatalonListeners();
			}
			window.__removeKatalonListeners = () => {
				element.removeEventListener('input', inputListener, false);
				element.removeEventListener('change', changeListener, false);
				element.removeEventListener('input', inputCaptureListener, true);
				element.removeEventListener('change', changeCaptureListener, true);
			}

			return signal;
		""", [element]);
	}

	/**
	 * - Verify if the value is actually set to the input
	 * - Verify if the onChange handlers can receive the event
	 */
	def checkVerifier(WebElement element, String desiredValue, String signal) {
		this.verifyInputListenable(signal);
		if (!this.verifyValueStable(element, desiredValue)) {
			return false;
		}
		return true;
	}

	def verifyValueStable(WebElement element, String desiredValue) {
		def currentValue = element.getAttribute('value');
		if (!currentValue.equals(desiredValue)) {
			return false;
		}

		// Try to unfocus and focus again
		this.unfocusElement();
		element.click();

		def valueNow = element.getAttribute('value');
		return valueNow.equals(desiredValue);
	}

	def verifyInputListenable(String signal) {
		Map<String, Boolean> result = WebUI.executeJavaScript("""
			var signal = arguments[0];
			var signalStore = window.__katalonSignals || {};
			var result = {
				input: signalStore[signal + '_input'],
				inputCapture: signalStore[signal + '_inputCapture'],
				change: signalStore[signal + '_change'],
				changeCapture: signalStore[signal + '_changeCapture'],
			}
			if (window.__removeKatalonListeners) {
				window.__removeKatalonListeners();
			}
			return result;
		""", [signal]);
		boolean ok = true;
		if (!result.get('input')) {
			ok = false;
			KeywordUtil.logInfo('> WARN: Cannot capture the "input" event');
		}
		if (!result.get('inputCapture')) {
			ok = false;
			KeywordUtil.logInfo('> WARN: Cannot capture the "input" event (capture phase)');
		}
		if (!result.get('change')) {
			ok = false;
			KeywordUtil.logInfo('> WARN: Cannot capture the "change" event');
		}
		if (!result.get('changeCapture')) {
			ok = false;
			KeywordUtil.logInfo('> WARN: Cannot capture the "change" event (capture phase)');
		}
		return ok;
	}
}


