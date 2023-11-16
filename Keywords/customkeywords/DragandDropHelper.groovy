package customkeywords

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
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords

import internal.GlobalVariable

public class DragandDropHelper {

	private static String getJsDndHelper() {
		return '''
		async function simulateDragDrop(sourceNode, destinationNode) {
		    var EVENT_TYPES = {
		        DRAG_START: 'dragstart',
				DRAG_ENTER: 'dragenter',
				DRAG_OVER: 'dragover',
		        DRAG_END: 'dragend',
		        DROP: 'drop'
		    }
		
		    function createCustomEvent(type) {
		        var event = new CustomEvent("CustomEvent")
		        event.initCustomEvent(type, true, true, null)
		        event.dataTransfer = {
					effectAllowed: "move",
					dropEffect: "none",
		            data: {
		            },
					types: [],
		            setData: function(type, val) {console.log("setdata", type);
		                this.data[type] = val;
						this.types.push(type);
		            },
		            getData: function(type) {
		                return this.data[type]
		            }
		        }
		        return event
		    }
		
		    function dispatchEvent(node, type, event) {
                var rect = node.getBoundingClientRect();
				event.clientX = rect.x + (rect.width / 2);
				event.clientY = rect.y + (rect.height / 2);
		        if (node.dispatchEvent) {
		            return node.dispatchEvent(event)
		        }
		        if (node.fireEvent) {
		            return node.fireEvent("on" + type, event)
		        }
		    }

			function delay(timeout) {
				return new Promise((resolve) => setTimeout(resolve, timeout));
			}
			var timeout = 1000
		
		    var dragStartEvent = createCustomEvent(EVENT_TYPES.DRAG_START)
		    dispatchEvent(sourceNode, EVENT_TYPES.DRAG_START, dragStartEvent)
			await delay(timeout)

		    var dragEnterEvent = createCustomEvent(EVENT_TYPES.DRAG_ENTER)
		    dispatchEvent(destinationNode, EVENT_TYPES.DRAG_ENTER, dragEnterEvent)
			await delay(timeout)

		    var dragOverEvent = createCustomEvent(EVENT_TYPES.DRAG_OVER)
		    dispatchEvent(destinationNode, EVENT_TYPES.DRAG_OVER, dragOverEvent)
			await delay(timeout)
		
		    var dropEvent = createCustomEvent(EVENT_TYPES.DROP)
		    dropEvent.dataTransfer = dragStartEvent.dataTransfer
		    dispatchEvent(destinationNode, EVENT_TYPES.DROP, dropEvent)
			await delay(timeout)
		
		    var dragEndEvent = createCustomEvent(EVENT_TYPES.DRAG_END)
		    dragEndEvent.dataTransfer = dragStartEvent.dataTransfer
		    dispatchEvent(sourceNode, EVENT_TYPES.DRAG_END, dragEndEvent)
			await delay(timeout)
		}
		''';
	}

	@Keyword
	public static dragAndDrop(TestObject sourceObject, TestObject destinationObject) {
		WebElement sourceElement = WebUiBuiltInKeywords.findWebElement(sourceObject);
		WebElement destinationElement = WebUiBuiltInKeywords.findWebElement(destinationObject);
		WebDriver webDriver = DriverFactory.getWebDriver();
		((JavascriptExecutor) webDriver).executeScript(getJsDndHelper() + "simulateDragDrop(arguments[0], arguments[1])", sourceElement, destinationElement)
	}
}
