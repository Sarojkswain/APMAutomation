package com.ca.apm.nextgen.tests.helpers.dashboard;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.Point;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kPathFillAttribute;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kPathStrokeAttribute;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kPolygonWidgetType;

/**
 * <code> DirectionalLineWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class DirectionalLineWidgetTester extends ADashboardWidgetTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectionalLineWidgetTester.class);

    private static final int kLineWidth = 1;
    private static final int kArrowWidth = 8;
    private static final int kArrowRadius = kArrowWidth / 2;
    private static final String kLineSpriteId = "-line";
    private static final String kStartArrowSpriteId = "-start-arrow";
    private static final String kEndArrowSpriteId = "-end-arrow";
    private static final Integer kStartArrowMode = 1;
    private static final Integer kEndArrowMode = 2;
    private int fArrowMode = 0;
    private List<Point> fPointList = new ArrayList<>();
    private Pattern fPattern = Pattern.compile("[M|L] *(\\d+\\.?\\d*)[, ] *(\\d+\\.?\\d*)",
        Pattern.CASE_INSENSITIVE);

    public DirectionalLineWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
        fArrowMode = fWidgetConfig.getIntegerPropertyValue("ArrowMode");
        fPointList = convertToRelativePoints(fWidgetConfig.getPointListProperties());
    }

    @Override
    public String getIdentifier() {
        return "dashboard-" + fWidgetConfig.getType();
    }

    @Override
    public boolean verifyGeometry() {
        boolean status;
        List<Point> arrowPoints;

        // verify overall geometry
        status = super.verifyGeometry();

        // verify line geometry
        status &= verifyLineGeometry(fPointList, kLineSpriteId);

        // verify arrow geometry if start arrow configured
        if ((fArrowMode & kStartArrowMode) > 0) {
            arrowPoints =
                makeArrowPoints(fPointList.get(0), getAngle(fPointList.get(1), fPointList.get(0)));
            status &= verifyLineGeometry(arrowPoints, kStartArrowSpriteId);
        }

        // verify arrow geometry if end arrow configured
        if ((fArrowMode & kEndArrowMode) > 0) {
            arrowPoints =
                makeArrowPoints(
                    fPointList.get(fPointList.size() - 1),
                    getAngle(fPointList.get(fPointList.size() - 2),
                        fPointList.get(fPointList.size() - 1)));
            status &= verifyLineGeometry(arrowPoints, kEndArrowSpriteId);
        }
        return status;
    }

    @Override
    public boolean verifyOptions() {
        boolean status = true;
        String configFrameColor = fWidgetConfig.getFrameColor();
        String browserFrameColor = getLineSpriteAttribute(kPathStrokeAttribute);
        status &= testStringValues(browserFrameColor, configFrameColor, kPathStrokeAttribute);

        // verify fill color if Polygon
        if (fWidgetConfig.getType().equals(kPolygonWidgetType)) {
            String configFillColor = fWidgetConfig.getFillColor();
            String browserFillColor = getLineSpriteAttribute(kPathFillAttribute);
            status &= testStringValues(browserFillColor, configFillColor, kPathFillAttribute);
        }
        return status;
    }

    /**
     * Calculates overall surface width of line widget given its list of points.
     * Initially set width equal to maximum change in X-axis.<br>
     * <br>
     * 
     * Adjust to line width if width is less than line width. <br>
     * Adjust for arrow width if widget has arrows
     * (i.e. widget is connector, elbow connector, or line with arrows)
     * and absolute value of slope is greater than or equal to 1.0
     * which means arrow will spill beyond maximum change in X-axis
     * 
     * @param pointList
     *        - list of points defining line widget
     * @return surface width of line widget
     */
    @Override
    public Integer getWidth() {
        Integer width = fWidgetConfig.getWidth();
        if (width < kLineWidth) {
            width = kLineWidth;
        }
        // line w/ arrows or connectors
        if (fArrowMode > 0) {
            double absoluteSlope = getAbsoluteSlope(fPointList.get(0), fPointList.get(1));
            if (Double.isNaN(absoluteSlope) || absoluteSlope >= 1.0) {
                width += kArrowWidth;
            }
        }
        return width;
    }

    /**
     * Calculates overall surface height of line widget given its list of points.
     * Initially set height equal to maximum change in Y-axis.<br>
     * <br>
     * 
     * Adjust to line width if height is less than line width.<br>
     * Adjust for arrow width if widget has arrows
     * (i.e. widget is connector, elbow connector, or line with arrows)
     * and absolute value of slope is less than 1.0
     * which means arrow will spill beyond maximum change in Y-axis
     * 
     * @param pointList - list of points defining line widget
     * @return surface height of line widget
     */
    @Override
    public Integer getHeight() {
        Integer height = fWidgetConfig.getHeight();
        if (height < kLineWidth) {
            height = kLineWidth;
        }
        // line w/ arrows or connectors
        if (fArrowMode > 0) {
            double absoluteSlope = getAbsoluteSlope(fPointList.get(0), fPointList.get(1));
            if (!Double.isNaN(absoluteSlope) && absoluteSlope < 1.0) {
                height += kArrowWidth;
            }
        }
        return height;
    }

    /**
     * Calculates the absolute value of slope given two points
     * 
     * @param point1 first PrecisePoint
     * @param point2 second PrecisePoint
     * 
     * @return double - returns 1 if the change in X-axis is 0
     *         otherwise returns absolute value of the change in Y-axis / change in X-axis
     */
    private double getAbsoluteSlope(Point point1, Point point2) {
        double x1 = point1.getXCoordinate();
        double y1 = point1.getYCoordinate();
        double x2 = point2.getXCoordinate();
        double y2 = point2.getYCoordinate();
        if (x2 - x1 == 0.0) {
            return Double.NaN;
        } else if (y2 - y1 == 0.0) {
            return 0;
        } else {
            return Math.abs((y2 - y1) / (x2 - x1));
        }
    }

    private String getLineSpriteAttribute(String attribute) {
        String value = null;
        WebElement pathDiv = ui.getWebElement(By.id(getId() + kLineSpriteId));
        if (pathDiv != null) {
            value = rgbToHex(ui.getWebElementAttribute(pathDiv, attribute).trim());
        }
        return value;
    }

    private boolean verifyLineGeometry(List<Point> configPoints, String spriteId) {
        boolean status = false;
        List<Point> browserPoints = getSpritePoints(spriteId);
        status = browserPoints.size() == configPoints.size();
        LOGGER.info("{}", this.getId() + spriteId);
        if (status) {
            for (int i = 0; i < browserPoints.size(); i++) {
                if (browserPoints.get(i).equals(configPoints.get(i))) {
                    LOGGER.info("Browser Sprite Path point({},{}) equals Configured point({},{})",
                        browserPoints.get(i).getXCoordinate(),
                        browserPoints.get(i).getYCoordinate(), configPoints.get(i).getXCoordinate(),
                        configPoints.get(i).getYCoordinate());
                } else {
                    LOGGER.error(
                        "Browser Sprite Path point({},{}) does not equal Configured point({},{})",
                        browserPoints.get(i).getXCoordinate(),
                        browserPoints.get(i).getYCoordinate(), configPoints.get(i).getXCoordinate(),
                        configPoints.get(i).getYCoordinate());
                    status = false;
                }
            }
        }
        return status;
    }

    private List<Point> getSpritePoints(String spriteId) {
        List<Point> pointList = new ArrayList<>();
        WebElement pathDiv = ui.getWebElement(By.id(getId() + spriteId));
        if (pathDiv != null) {
            String pathString = pathDiv.getAttribute("d");
            Matcher m = fPattern.matcher(pathString);
            while (m.find()) {
                double x = Double.valueOf(m.group(1));
                double y = Double.valueOf(m.group(2));
                pointList.add(fWidgetConfig.createPoint(x, y));
            }
        }
        return pointList;
    }

    /**
     * Converts list of connecting points to be relative to the origin of the surface
     * that will contain points
     * 
     * @param points - list of points that are connected to form line widget
     * @return list of points adjusted to surface origin
     */
    private List<Point> convertToRelativePoints(List<Point> points) {
        List<Point> relativePoints = new ArrayList<>();
        Point origin = getSurfaceOrigin(points);
        for (Point point : points) {
            double x = point.getXCoordinate() - origin.getXCoordinate();
            double y = point.getYCoordinate() - origin.getYCoordinate();
            relativePoints.add(fWidgetConfig.createPoint(x, y));
        }
        return relativePoints;
    }

    public Point getSurfaceOrigin(List<Point> pointList) {
        Integer minX = Integer.MAX_VALUE;
        Integer minY = Integer.MAX_VALUE;
        for (Point point : pointList) {
            if (point.getXCoordinate() < minX) {
                minX = (int) point.getXCoordinate();
            }
            if (point.getYCoordinate() < minY) {
                minY = (int) point.getYCoordinate();
            }
        }
        // line w/ arrows or connectors
        if (fArrowMode > 0) {
            double absoluteSlope = getAbsoluteSlope(pointList.get(0), pointList.get(1));
            if (!Double.isNaN(absoluteSlope) && absoluteSlope < 1.0) {
                minY -= kArrowWidth / 2;
            } else {
                minX -= kArrowWidth / 2;
            }
        }
        return fWidgetConfig.createPoint(minX, minY);
    }

    /**
     * Calculates angle of line from point1 to point2
     * assuming points are relative to screen origin (top,left) <br>
     * <br>
     * Converts change in screen point coordinates
     * to angle in Cartesian coordinate system where origin is (bottom,left)
     * 
     * @param point1 starting PrecisePoint (based on Screen top, left origin)
     * @param point2 ending PrecisePoint (based on Screen top, left origin)
     * @return angle in degrees (0-359) based on Cartesian origin (bottom, left)
     */
    private double getAngle(Point point1, Point point2) {
        double angle = 0;
        double angleDegrees = 90;

        double absoluteSlope = getAbsoluteSlope(point1, point2);
        if (!Double.isNaN(absoluteSlope)) {
            angleDegrees = Math.toDegrees(Math.atan(absoluteSlope));
        }

        double deltaX = point2.getXCoordinate() - point1.getXCoordinate();
        double deltaY = point2.getYCoordinate() - point1.getYCoordinate();

        // Convert every combination of X,Y changes to angle
        if (deltaX == 0.0 && deltaY == 0.0) {
            angle = 0; // should not happen
        } else if (deltaX == 0.0 && deltaY < 0) {
            angle = 90; // maps to Cartesian quadrant 1
        } else if (deltaX == 0.0 && deltaY > 0) {
            angle = 270; // maps to Cartesian quadrant 3
        } else if (deltaX < 0.0 && deltaY == 0.0) {
            angle = 180; // maps to Cartesian quadrant 2
        } else if (deltaX < 0.0 && deltaY < 0.0) {
            angle = 180 - angleDegrees; // maps to Cartesian quadrant 2
        } else if (deltaX < 0.0 && deltaY > 0.0) {
            angle = 180 + angleDegrees; // maps to Cartesian quadrant 3
        } else if (deltaX > 0.0 && deltaY == 0.0) {
            angle = 0; // maps to Cartesian quadrant 1
        } else if (deltaX > 0.0 && deltaY < 0.0) {
            angle = angleDegrees; // maps to Cartesian quadrant 1
        } else if (deltaX > 0.0 && deltaY > 0.0) {
            angle = 360 - angleDegrees; // maps to Cartesian quadrant 4
        }
        return angle;
    }

    public List<Point> makeArrowPoints(Point endPoint, double angleDegrees) {
        List<Point> arrowPoints = new ArrayList<>();
        double angleRadians = Math.toRadians(angleDegrees);
        // calculate circle origin relative to end point and its angle
        Point circleOrigin =
            fWidgetConfig.createPoint(endPoint.getXCoordinate() - Math.cos(angleRadians)
                * kArrowRadius, endPoint.getYCoordinate() + Math.sin(angleRadians) * kArrowRadius);
        arrowPoints.add(makeUnitCirclePoint(circleOrigin, angleDegrees, kArrowRadius));
        arrowPoints.add(makeUnitCirclePoint(circleOrigin, angleDegrees + 135, kArrowRadius));
        arrowPoints.add(makeUnitCirclePoint(circleOrigin, angleDegrees + 225, kArrowRadius));
        return arrowPoints;
    }

    /**
     * Creates a PrecisePoint (x,y coordinate) relative to the origin point of a circle
     * given an angle, and the radius of the circle<br>
     * <br>
     * Calculates x and y coordinates on a unit circle given the angle,
     * multiplies each coordinate by the circle radius and adds each value to
     * the matching coordinate in the circle origin.
     * 
     * @param circleOrigin - origin of circle PrecisePoint (x,y coordinate)
     * @param angleDegrees - angle of point on circle
     * @param radius - radius of circle
     * @return PrecisePoint (x,y coordinate)
     */
    private Point makeUnitCirclePoint(Point circleOrigin, double angleDegrees, int radius) {
        double degrees = angleDegrees;
        degrees = degrees > 360 ? degrees - 360 : degrees;
        double angleRadians = Math.toRadians(degrees);
        double x = circleOrigin.getXCoordinate() + Math.cos(angleRadians) * radius;
        double y = circleOrigin.getYCoordinate() - Math.sin(angleRadians) * radius;
        return fWidgetConfig.createPoint(x, y);
    }

}
