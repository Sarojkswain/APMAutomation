package com.ca.apm.saas.test;

import static com.ca.apm.test.atc.common.element.WebElementWrapper.wrapElement;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class BAMapViewTest extends SaaSBaseTest {
	private static final Logger logger = LoggerFactory.getLogger(BAMapViewTest.class);
	
	@Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.HUB_ROLE_ID), size = SizeType.MEDIUM, owner = "banra06")
	@Test
	public void verifyMapView() throws Exception {

		attemptLogin(ui.getDriver());
		MapPage mapHelper = new MapPage(ui.getDriver());
		mapHelper.clickMapView(10000);
		mapHelper.clickLiveTimeRange();
		String filter = "ca apm demo host";
		mapHelper.addNewMapFilter("Hostname", new String[] { filter });
		final String perspective = "No Perspective";
		try {
			if (!ui.getPerspectivesControl().isPerspectiveActive(perspective)) {
				ui.getPerspectivesControl()
						.selectPerspectiveByName(perspective);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}		
		Canvas canvas = ui.getCanvas();
		canvas.waitForUpdate();
		String[] nodes = { "/ReportingService/ServletA6",
				"/ReportingService/ServletA7", "/TradingService/TradeOptions",
				"/ReportingService/ServletA8",
				"/TradingService/PlaceOrder/mobile",
				"/TradingService/PlaceOrder",
				"/AuthenticationService/ServletF8/mobile",
				"/AuthenticationService/ServletF8",
				"/TradingService/ViewOrders" };
		for (String nodeName : nodes)
			verifyTraces(canvas, nodeName, mapHelper);
	}

	private void verifyTraces(Canvas canvas, String nodeToSelect,
			MapPage mapHelper) throws Exception {
		logger.info("Checking transaction traces and metrics for -----"+ nodeToSelect);
		canvas.selectNodeByName(nodeToSelect);
		canvas.getCtrl().fitSelectedToView();
		validateBAData(mapHelper, nodeToSelect);

		WebElement bottomDrawerButton = Utils
				.waitForCondition(
						ui.getDriver(),
						ExpectedConditions.visibilityOfElementLocated(By
								.xpath("//*[contains(@id, 'bottomDrawerMaximizeButton')]")),
						60);
		new Actions(ui.getDriver()).moveToElement(bottomDrawerButton).click()
				.perform();

		WebElement transactionTracesTabHandle = Utils.waitForCondition(ui
				.getDriver(), ExpectedConditions.visibilityOfElementLocated(By
				.xpath("//a[contains(text(), 'Business Transactions')]")), 60);

		new Actions(ui.getDriver()).moveToElement(transactionTracesTabHandle)
				.click().perform();

		PageElement transactionTraceTab = wrapElement(
				Utils.waitUntilVisible(ui.getDriver(),
						By.className("tab-content")), ui);
		PageElement transactionTraceViewer = wrapElement(
				transactionTraceTab
						.findElement(By
								.xpath("//*[contains(@id, 'transaction-trace-viewer')]")),
				ui);
		Utils.waitUntilVisible(ui.getDriver(),
				By.xpath("//*[contains(@id, 'transaction-trace-viewer')]"));
		PageElement gridTransactionTraces = transactionTraceViewer
				.findElement(By
						.xpath("//*[contains(@id,'gridTransactionTraces')]"));
		WebElement table = getTable(gridTransactionTraces);
		WebElement oneDurationBar = table.findElement(By
				.cssSelector("div.tt-instance-bar"));
		new Actions(ui.getDriver()).moveToElement(oneDurationBar).click()
				.perform();
		Utils.waitForCondition(ui.getDriver(), ExpectedConditions
				.visibilityOfElementLocated(By
						.xpath("//*[contains(@id, 'tt-segment')]")), 60);
		canvas.getCanvas().scrollToTop();
		canvas.clickToUnusedPlaceInCanvas();
		Thread.sleep(10000);		
	}

	private static WebElement getTable(SearchContext gridTransactionTraces) {
		return gridTransactionTraces
				.findElement(By
						.cssSelector("div.ui-grid-contents-wrapper div[role=grid] div.ui-grid-viewport[role=rowgroup]"
								+ " div.ui-grid-canvas"));
	}

	protected void validateBAData(MapPage mapHelper, String nodeName)
			throws Exception {
		String[] data = new String[] { "Average Response Time (ms)",
				"Responses Per Interval", "Average Page Stall Time (ms)",
				"Average Page Render Time (ms)",
				"Average Connection Establishment Time (ms)",
				"Average Domain Lookup Time (ms)",
				"Average DOM Processing Time (ms)",
				"Average Previous Page Unload Time (ms)",
				"Average Page Load Time (ms)",
				"Average Time to First Byte (ms)",
				"Average Time to Last Byte (ms)", "Page Hits Per Interval" };
		for (String metric : data) {
			Assert.assertTrue(mapHelper.isMetricPresent(metric),
					"Metric/Attribute '" + metric + " doesn't exist");
		}
	}
}