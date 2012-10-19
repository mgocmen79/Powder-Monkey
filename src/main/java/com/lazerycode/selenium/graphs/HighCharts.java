package com.lazerycode.selenium.graphs;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

public class HighCharts {

  private int waitTimeoutInSeconds = 15;
  private WebDriver driver;
  private WebElement chart;
  private WebDriverWait wait;
  private Mouse mouse;
  private Actions performAction;
  private JavascriptLibrary javascript = new JavascriptLibrary();

  @FindBy(how = How.CSS, using = "g.highcharts-tooltip")
  private WebElement toolTip;
  @FindBy(how = How.CSS, using = "g.highcharts-legend")
  private WebElement legend;
  @FindBy(how = How.CSS, using = "g.highcharts-axis-labels")
  private List<WebElement> axisLabels;

  public HighCharts(WebDriver driver, WebElement chart) {
    PageFactory.initElements(new DefaultElementLocatorFactory(chart), this);
    this.driver = driver;
    this.chart = chart;

    wait = new WebDriverWait(driver, waitTimeoutInSeconds, 100);
    mouse = ((HasInputDevices) driver).getMouse();
    performAction = new Actions(driver);
  }

  public boolean isChartDisplayed() {
    return wait.until(visibilityOf(this.chart)) != null;
  }

  private WebElement getXAxisLabels() {
    return axisLabels.get(0);
  }

  public List<String> getXAxisLabelsText() {
    List<String> labels = new ArrayList<String>();
    List<WebElement> xAxisLabels = getXAxisLabels().findElements(By.cssSelector("text"));
    for (int i = 0; i < xAxisLabels.size(); i++) {
      WebElement xAxisLabel = xAxisLabels.get(i);
      labels.add(xAxisLabel.getText());
    }
    return labels;
  }

  private WebElement getYAxisLabels() {
    return axisLabels.get(1);
  }

  public List<String> getYAxisLabelsText() {
    List<String> labels = new ArrayList<String>();
    List<WebElement> yAxisLabels = getYAxisLabels().findElements(By.cssSelector("text"));
    for (int i = 0; i < yAxisLabels.size(); i++) {
      WebElement yAxisLabel = yAxisLabels.get(i);
      labels.add(yAxisLabel.getText());
    }
    return labels;
  }

  public boolean isLegendDisplayed() {
    return legend.isDisplayed();
  }


  public String getToolTipLine(int lineNo) throws NoSuchElementException {
    List<String> lines = new ArrayList<String>();
    List<WebElement> toolTipLines = toolTip.findElements(By.cssSelector("text tspan"));
    for (int i = 0; i < toolTipLines.size(); i++) {
      WebElement toolTipLine = toolTipLines.get(i);
      lines.add(toolTipLine.getText());
    }
    if (lineNo >= lines.size()) {
      throw new NoSuchElementException("There is no line " + lineNo + "! There are only " + lines.size() + " lines in the tool tip");
    }
    return lines.get(lineNo);
  }

  public void hoverOverBarChartSeriesAtXAxisPosition(int series, String xAxisLabel) {
    int barNumber = getXAxisLabelsText().indexOf(xAxisLabel);
    WebElement pointToHoverOver = chart.findElements(By.cssSelector("g.highcharts-tracker > g:nth-of-type(" + series + ") > rect")).get(barNumber);
    javascript.callEmbeddedSelenium(driver, "triggerEvent", pointToHoverOver, "mouseover");
  }

  public void hoverOverPointAtXAxisPositionForLineChart(String xAxisLabelValue) {
    int barNumber = getXAxisLabelsText().indexOf(xAxisLabelValue);
    hoverOverPointAtXAxisPositionForLineChartDataPointIndex(barNumber);
  }

  public void hoverOverPointAtXAxisPositionForLineChartDataPointIndex(int barNumber) {
    //Find x position of chart label
    WebElement xAxisLabel = getXAxisLabels().findElements(By.cssSelector("text")).get(barNumber);
    int xPositionOfLabel = extractXAttributeAsInteger(xAxisLabel);

    //Get left most point of line on graph
    WebElement firstItemInDataSeries = getYAxisLabels().findElement(By.cssSelector("text"));
    int dataSeriesLeftMostPoint = extractXAttributeAsInteger(firstItemInDataSeries);

    int hoverPoint = xPositionOfLabel - dataSeriesLeftMostPoint;

    WebElement elementToHoverOver = chart.findElement(By.cssSelector("g.highcharts-tracker > g > path"));

    javascript.callEmbeddedSelenium(driver, "triggerEvent", elementToHoverOver, "mouseover");
    performAction.moveToElement(elementToHoverOver).moveByOffset(hoverPoint, 0);
    //TODO Check the above is working with IE and we don't need the hack below
//    mouse.mouseMove(((Locatable) elementToHoverOver).getCoordinates());
//    mouse.mouseMove(((Locatable) elementToHoverOver).getCoordinates(), hoverPoint, 0);
  }

  private int extractXAttributeAsInteger(WebElement xAxisLabel) {
    Double xAttribute = Double.parseDouble(xAxisLabel.getAttribute("x"));
    return xAttribute.intValue();
  }

  public boolean isTooltipDisplayed() {
    return wait.until(attributeIsEqualTo(toolTip, "visibility", "visible"));
  }

  public static ExpectedCondition<Boolean> attributeIsEqualTo(final WebElement element, final String attribute, final String attributeValue) {
    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver driver) {
        return element.getAttribute(attribute).equals(attributeValue);
      }
    };
  }
}