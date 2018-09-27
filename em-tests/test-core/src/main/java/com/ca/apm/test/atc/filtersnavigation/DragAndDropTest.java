package com.ca.apm.test.atc.filtersnavigation;

import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.BottomBar;
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.UI;

public class DragAndDropTest extends UITest {

    private static final Logger logger = Logger.getLogger(DragAndDropTest.class);

    private UI ui;
    private FilterBy f;
    private BottomBar b;

    private class Item {
        private final String name;
        private final int btGroupId;

        public Item(String name, int btGroupId) {
            this.name = name;
            this.btGroupId = btGroupId;
        }

        public Item(String name) {
            this(name, FilterMenu.NOT_IN_BT_COVERAGE);
        }

        public String getName() {
            return name;
        }

        public int getBtGroupId() {
            return btGroupId;
        }

        public boolean isInBtGroup() {
            return btGroupId != FilterMenu.NOT_IN_BT_COVERAGE;
        }
    }

    /**
     * Assign any existing attributes on which it can be filtered
     */
    private static final String A = "Type";
    private static final String B = "Name";
    private static final String C = "Hostname";
    private static final String D = "Application";
    private static final String E = "Business Service";
    private static final String F = "wsOperation";
    private static final String H = "Business Transaction";
    private static final String I = "wsNamespace";
    private static final String J = "agentDomain";

    private final Item A_ng = new Item(A);
    private final Item B_ng = new Item(B);
    private final Item C_ng = new Item(C);
    private final Item D_ng = new Item(D);
    private final Item E_ng = new Item(E);
    private final Item F_ng = new Item(F);
    private final Item H_ng = new Item(H);
    private final Item I_ng = new Item(I);
    private final Item J_ng = new Item(J);

    private final Item A_g1 = new Item(A, 1);
    private final Item B_g1 = new Item(B, 1);
    private final Item C_g1 = new Item(C, 1);
    private final Item D_g1 = new Item(D, 1);
    private final Item H_g1 = new Item(H, 1);
    private final Item I_g1 = new Item(I, 1);
    private final Item J_g1 = new Item(J, 1);

    private final Item B_g2 = new Item(B, 2);
    private final Item D_g2 = new Item(D, 2);
    private final Item E_g2 = new Item(E, 2);
    private final Item F_g2 = new Item(F, 2);
    
    private final Item E_g3 = new Item(E, 3);
    private final Item F_g3 = new Item(F, 3);
    private final Item H_g3 = new Item(H, 3);
    private final Item I_g3 = new Item(I, 3);
    private final Item J_g3 = new Item(J, 3);

    private void initialize() throws Exception {
        ui = getUI();
        f = ui.getFilterBy();
        b = ui.getBottomBar();

        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();
    }

    private void verifyFilter(Item[][] filterItems) throws Exception {
        int totalItemsCount = 0;
        int clausesCount = filterItems.length;
        Assert.assertEquals(f.getFilterClausesCount(), clausesCount,
            "Unmatched number of disjunctive clauses");

        for (int i = 0; i < clausesCount; i++) {
            int itemsCount = filterItems[i].length;
            totalItemsCount += itemsCount; 
            Assert.assertEquals(f.getFilterItemCount(i), itemsCount,
                "Unmatched number of items in the disjunctive clause #" + i);

            List<FilterMenu> items = f.getFilterItemObjects(i);
            for (int j = 0; j < itemsCount; j++) {
                Assert
                    .assertEquals(items.get(j).getName(), filterItems[i][j].getName(),
                        "Unmatched filter item at the index #" + j + " in the disjunctive clause #"
                            + i);

                Assert.assertEquals(items.get(j).getBtGroupId(), filterItems[i][j].getBtGroupId(),
                    "Unmatched BT Coverage Group ID of the filter item '" + items.get(j).getName()
                        + "' at the index #" + j + " in the disjunctive clause #" + i);
            }
        }
        
        Assert.assertEquals(b.getActiveFilterCount(), totalItemsCount, "Active filter count in the bottom bar does not match the expected value after a drag'n'drop operation");
    }

    private void prepareFilters1() throws Exception {
        logger.info("prepare filter items 1");

        // data-filter-id=1
        f.add(A_ng.getName(), 0, A_ng.isInBtGroup(), false);

        // data-filter-id=2
        f.add(B_ng.getName(), 0, B_ng.isInBtGroup(), false);

        // data-filter-id=3
        f.add(C_ng.getName(), 0, C_ng.isInBtGroup(), false);

        // data-filter-id=4
        f.add(D_ng.getName(), 0, D_ng.isInBtGroup(), false);

        // data-filter-id=5 --- OR ---
        f.add(E_ng.getName(), 0, E_ng.isInBtGroup(), true);

        // data-filter-id=6 --- OR ---
        f.add(F_ng.getName(), 1, F_ng.isInBtGroup(), true);

        // data-filter-id=7
        f.add(H_ng.getName(), 2, H_ng.isInBtGroup(), false);

        // data-filter-id=8
        f.add(I_ng.getName(), 2, I_ng.isInBtGroup(), false);
        
        Assert.assertEquals(b.getActiveFilterCount(), 8, "Active filter count in the bottom bar does not match the expected value");
    }

    private void prepareFilters2() throws Exception {
        logger.info("prepare filter items 2");

        // data-filter-id=1
        f.add(A_ng.getName(), 0, A_ng.isInBtGroup(), false);

        // data-filter-id=2
        f.add(B_ng.getName(), 0, B_ng.isInBtGroup(), false);

        // data-filter-id=3, bt-data=1
        f.add(C_g1.getName(), 0, C_g1.isInBtGroup(), false);

        // data-filter-id=4 --- OR ---
        f.add(D_ng.getName(), 0, D_ng.isInBtGroup(), true);

        // data-filter-id=5, bt-data=2
        f.add(E_g2.getName(), 1, E_g2.isInBtGroup(), false);

        // data-filter-id=6
        f.add(F_ng.getName(), 1, F_ng.isInBtGroup(), false);

        // data-filter-id=7, bt-data=3 --- OR ---
        f.add(H_g3.getName(), 1, H_g3.isInBtGroup(), true);

        // data-filter-id=8
        f.add(I_ng.getName(), 2, I_ng.isInBtGroup(), false);

        // data-filter-id=9
        f.add(J_ng.getName(), 2, J_ng.isInBtGroup(), false);
        
        Assert.assertEquals(b.getActiveFilterCount(), 9, "Active filter count in the bottom bar does not match the expected value");
    }
    
    private void prepareFilters3() throws Exception {
        logger.info("prepare filter items 3");

        // data-filter-id=1, bt-data=1
        f.add(A_g1.getName(), 0, A_g1.isInBtGroup(), false);

        // data-filter-id=2, bt-data=1
        f.addToBtGroup(B_g1.getName(), 1);

        // data-filter-id=3, bt-data=1
        f.addToBtGroup(C_g1.getName(), 1);

        // data-filter-id=4, bt-data=2 
        f.add(D_g2.getName(), 0, D_g2.isInBtGroup(), false);

        // data-filter-id=5, bt-data=2
        f.addToBtGroup(E_g2.getName(), 2);

        // data-filter-id=6, bt-data=3  --- OR ---
        f.add(F_g3.getName(), 0, F_g3.isInBtGroup(), true);

        // data-filter-id=7, bt-data=3 
        f.addToBtGroup(H_g3.getName(), 3);

        // data-filter-id=8, bt-data=3
        f.addToBtGroup(I_g3.getName(), 3);

        // data-filter-id=9, bt-data=3
        f.addToBtGroup(J_g3.getName(), 3);
        
        Assert.assertEquals(b.getActiveFilterCount(), 9, "Active filter count in the bottom bar does not match the expected value");
    }

    private void prepareFilters4() throws Exception {
        logger.info("prepare filter items 4");

        // data-filter-id=1
        f.add(A_ng.getName(), 0);

        // data-filter-id=2
        f.add(B_ng.getName(), 0);

        // data-filter-id=3, bt-data=1
        f.add(C_g1.getName(), 0, true, true);
        
        Assert.assertEquals(b.getActiveFilterCount(), 3, "Active filter count in the bottom bar does not match the expected value");
    }

    private void prepareFilters5() throws Exception {
        logger.info("prepare filter items 5");

        // data-filter-id=1
        f.add(A_ng.getName(), 0);

        // data-filter-id=2
        f.add(B_ng.getName(), 0);

        // data-filter-id=3
        f.add(C_ng.getName(), 0, false, true);
        
        Assert.assertEquals(b.getActiveFilterCount(), 3, "Active filter count in the bottom bar does not match the expected value");
    }

    private void prepareFilters6() throws Exception {
        logger.info("prepare filter items 6");

        // data-filter-id=1
        f.add(A_ng.getName(), 0);

        // data-filter-id=2
        f.add(B_ng.getName(), 0);

        // data-filter-id=3
        f.add(C_g1.getName(), 0, true, true);
        
        // data-filter-id=4
        f.addToBtGroup(D_g1.getName(), 1);
        
        Assert.assertEquals(b.getActiveFilterCount(), 4, "Active filter count in the bottom bar does not match the expected value");
    }

    private void prepareFilters7() throws Exception {
        logger.info("prepare filter items 7");

        // data-filter-id=1
        f.add(A_ng.getName(), 0);

        // data-filter-id=2
        f.add(B_g1.getName(), 0, true, false);
        
        Assert.assertEquals(b.getActiveFilterCount(), 2, "Active filter count in the bottom bar does not match the expected value");
    }
    
    @Test
    public void testMove01ForwardInsideClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move B before D
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneBeforeItemSelector(4));

        /* verify */

        Item[][] res = { {A_ng, C_ng, B_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove02BackwardsInsideClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move D before B
        f.performDragAndDrop(f.getDraggableItemSelector(4), f.getDropZoneBeforeItemSelector(2));

        /* verify */

        Item[][] res = { {A_ng, D_ng, B_ng, C_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove03ForwardAcrossClauses() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move B before H
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneBeforeItemSelector(7));

        /* verify */

        Item[][] res = { {A_ng, C_ng, D_ng}, {E_ng}, {F_ng, B_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove04BackwardsAcrossClauses() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move I before D
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneBeforeItemSelector(4));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, I_ng, D_ng}, {E_ng}, {F_ng, H_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    /**
     * The first item of a clause bears the 'OR' operator (the very first item of the whole filter
     * can have either 'AND' or 'OR')
     * that effectively divides the filter expression.
     * If the item is moved forward, the 'OR' operator must be passed to the item that was next to
     * it on the right side.
     * 
     * @throws Exception
     */
    @Test
    public void testMove05FirstClauseItemForwardWithinClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move F before I
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneBeforeItemSelector(8));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {H_ng, F_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    /**
     * The first item of a clause bears the 'OR' operator (the very first item of the whole filter
     * can have either 'AND' or 'OR')
     * that effectively divides the filter expression.
     * If another item is moved to its place, the 'OR' operator must be passed to that item.
     * 
     * @throws Exception
     */
    @Test
    public void testMove06BackwardsToTheFirstPlaceWithinClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move I before F
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneBeforeItemSelector(6));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {I_ng, F_ng, H_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove07ItemOfSingleItemClauseBackwards() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move E before D
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(4));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, E_ng, D_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove08ItemOfSingleItemClauseBackwardsToEndOfPreviousClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move E after D
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneAfterItemSelector(4));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng, E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove09ItemOfSingleItemClauseForwards() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move E before H
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(7));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng}, {F_ng, E_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove10ItemOfSingleItemClauseForwardsToBeginOfNextClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move E before F
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(6));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng}, {E_ng, F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove11FirstClauseItemForwardToTheEndOfClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move A after D
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAfterItemSelector(4));

        /* verify */

        Item[][] res = { {B_ng, C_ng, D_ng, A_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove12ToBeginOfSingleItemClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move D before E
        f.performDragAndDrop(f.getDraggableItemSelector(4), f.getDropZoneBeforeItemSelector(5));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng}, {D_ng, E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove13ToEndOfSingleItemClause() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move F after E
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneAfterItemSelector(5));

        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng, D_ng}, {E_ng, F_ng}, {H_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove14ToTheVeryStart() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move H before A
        f.performDragAndDrop(f.getDraggableItemSelector(7), f.getDropZoneBeforeItemSelector(1));

        /* verify */

        Item[][] res = { {H_ng, A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove15ToTheVeryEnd() throws Exception {
        initialize();
        prepareFilters1();

        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        /* execute */

        // move B after I
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneAfterItemSelector(8));

        /* verify */

        Item[][] res = { {A_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng, B_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    /**
     * Please note: each filter item keep its initial ID even after it has been moved!!!
     * 
     * @throws Exception
     */
    @Test
    public void testMove16PerformSeriesOfActions() throws Exception {
        initialize();
        prepareFilters1();

        /**
         * <pre>
         *               1     2     3     4       5       6     7     8
         * </pre>
         */
        Item[][] in = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(in);

        // move B before F
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneBeforeItemSelector(6));
        Item[][] res1 = { {A_ng, C_ng, D_ng}, {E_ng}, {B_ng, F_ng, H_ng, I_ng}};
        verifyFilter(res1);

        // move F after D
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneAfterItemSelector(4));
        Item[][] res2 = { {A_ng, C_ng, D_ng, F_ng}, {E_ng}, {B_ng, H_ng, I_ng}};
        verifyFilter(res2);

        // move A after E
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAfterItemSelector(5));
        Item[][] res3 = { {C_ng, D_ng, F_ng}, {E_ng, A_ng}, {B_ng, H_ng, I_ng}};
        verifyFilter(res3);

        // move B before C
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneBeforeItemSelector(3));
        Item[][] res4 = { {B_ng, C_ng, D_ng, F_ng}, {E_ng, A_ng}, {H_ng, I_ng}};
        verifyFilter(res4);

        // move F before H
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneBeforeItemSelector(7));
        Item[][] res5 = { {B_ng, C_ng, D_ng}, {E_ng, A_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res5);

        // move A before B
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneBeforeItemSelector(2));
        Item[][] res6 = { {A_ng, B_ng, C_ng, D_ng}, {E_ng}, {F_ng, H_ng, I_ng}};
        verifyFilter(res6);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove17BeforeBtGroup() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move A before C
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneBeforeItemSelector(3));

        // move F before E
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneBeforeItemSelector(5));
        
        // move I before H
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneBeforeItemSelector(7));
        
        /* verify */

        Item[][] res = { {B_ng, A_ng, C_g1}, {D_ng, F_ng, E_g2}, {I_ng, H_g3, J_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove18AfterBtGroup() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move A after C
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAfterItemSelector(3));

        // move D after E (i.e. before F, as the new position is not at end of clause of line)
        f.performDragAndDrop(f.getDraggableItemSelector(4), f.getDropZoneBeforeItemSelector(6));
        
        // move J after H (i.e. before I, as the new position is not at end of clause of line)
        f.performDragAndDrop(f.getDraggableItemSelector(9), f.getDropZoneBeforeItemSelector(8));
        
        /* verify */

        Item[][] res = { {B_ng, C_g1, A_ng}, {E_g2, D_ng, F_ng}, {H_g3, J_ng, I_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove19IntoStartOfBtGroup() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move A into BT group 1 before C
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAtStartOfItemSelector(3));

        // move D into BT group 2 before E
        f.performDragAndDrop(f.getDraggableItemSelector(4), f.getDropZoneAtStartOfItemSelector(5));
        
        // move I into BT group 3 before H
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneAtStartOfItemSelector(7));
        
        /* verify */

        Item[][] res = { {B_ng, A_g1, C_g1}, {D_g2, E_g2, F_ng}, {I_g3, H_g3, J_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove20IntoEndOfBtGroup() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move A into BT group 1 after C
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAtEndOfItemSelector(3));

        // move D into BT group 2 after E 
        f.performDragAndDrop(f.getDraggableItemSelector(4), f.getDropZoneAtEndOfItemSelector(5));
        
        // move I into BT group 3 after H
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneAtEndOfItemSelector(7));
        
        /* verify */

        Item[][] res = { {B_ng, C_g1, A_g1}, {E_g2, D_g2, F_ng}, {H_g3, I_g3, J_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove21OutOfBtGroupLeftInPlace() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move C out of BT group 1
        f.performDragAndDrop(f.getDraggableItemSelector(3), f.getDropZoneBeforeItemSelector(3));

        // move E out of BT group 2  
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(5));
        
        // move H out of BT group 3
        f.performDragAndDrop(f.getDraggableItemSelector(7), f.getDropZoneBeforeItemSelector(7));
        
        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng}, {D_ng, E_ng, F_ng}, {H_ng, I_ng, J_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove22OutOfBtGroupRightInPlace() throws Exception {
        initialize();
        prepareFilters2();

        Item[][] in = { {A_ng, B_ng, C_g1}, {D_ng, E_g2, F_ng}, {H_g3, I_ng, J_ng}};
        verifyFilter(in);

        /* execute */

        // move C out of BT group 1
        f.performDragAndDrop(f.getDraggableItemSelector(3), f.getDropZoneAfterItemSelector(3));

        // move E out of BT group 2  
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(6));
        
        // move H out of BT group 3
        f.performDragAndDrop(f.getDraggableItemSelector(7), f.getDropZoneBeforeItemSelector(8));
        
        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng}, {D_ng, E_ng, F_ng}, {H_ng, I_ng, J_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }

    @Test
    public void testMove23AmongBtGroups() throws Exception {
        initialize();
        prepareFilters3();

        Item[][] in = { {A_g1, B_g1, C_g1, D_g2, E_g2}, {F_g3, H_g3, I_g3, J_g3}};
        verifyFilter(in);

        /* execute */

        // move B between D and E 
        f.performDragAndDrop(f.getDraggableItemSelector(2), f.getDropZoneBeforeItemSelector(5));

        // move E between I and J  
        f.performDragAndDrop(f.getDraggableItemSelector(5), f.getDropZoneBeforeItemSelector(9));
        
        // move H between A and C
        f.performDragAndDrop(f.getDraggableItemSelector(7), f.getDropZoneBeforeItemSelector(3));
        
        /* verify */

        Item[][] res = { {A_g1, H_g1, C_g1, D_g2, B_g2}, {F_g3, I_g3, E_g3, J_g3}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove24BetweenBtGroups() throws Exception {
        initialize();
        prepareFilters3();

        Item[][] in = { {A_g1, B_g1, C_g1, D_g2, E_g2}, {F_g3, H_g3, I_g3, J_g3}};
        verifyFilter(in);

        /* execute */

        // move H between C and D (i.e. between BT Groups 1 and 2)
        f.performDragAndDrop(f.getDraggableItemSelector(7), f.getDropZoneBeforeItemSelector(4));

        // move I after C (at the end BT Group 1)  
        f.performDragAndDrop(f.getDraggableItemSelector(8), f.getDropZoneAtEndOfItemSelector(3));
        
        // move F before D (at the start of BT Group 2)
        f.performDragAndDrop(f.getDraggableItemSelector(6), f.getDropZoneAtStartOfItemSelector(4));
        
        // move J before A (at the start of BT Group 1)
        f.performDragAndDrop(f.getDraggableItemSelector(9), f.getDropZoneAtStartOfItemSelector(1));
        
        /* verify */

        Item[][] res = { {J_g1, A_g1, B_g1, C_g1, I_g1, H_ng, F_g2, D_g2, E_g2}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    @Test
    public void testMove25OutOfBtGroupIfSingleInClause() throws Exception {
        initialize();
        prepareFilters4();

        Item[][] in = { {A_ng, B_ng}, {C_g1}};
        verifyFilter(in);

        /* execute */

        // move C right of the BT group 1
        f.performDragAndDrop(f.getDraggableItemSelector(3), f.getDropZoneAfterItemSelector(3));
        
        /* verify */

        Item[][] res = { {A_ng, B_ng}, {C_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    /**
     * Covering the fix of the issue DE130983
     * 
     * @throws Exception
     */
    @Test
    public void testMove26SingleItemInClauseToEndOfPrevious() throws Exception {
        initialize();
        prepareFilters5();

        Item[][] in = { {A_ng, B_ng}, {C_ng}};
        verifyFilter(in);

        /* execute */

        // move C right to the end of previous clause
        f.performDragAndDrop(f.getDraggableItemSelector(3), f.getDropZoneAfterItemSelector(2));
        
        /* verify */

        Item[][] res = { {A_ng, B_ng, C_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    /**
     * Covering the fix of the issue DE166407 - not able to move the first element from BT coverage box that is positioned on left of any additional OR clause to the end of filter
     * 
     * @throws Exception
     */
    @Test
    public void testMove27MoveLeftMostItemFromBtToTheEndOfFilter() throws Exception {
        initialize();
        prepareFilters6();

        Item[][] in = { {A_ng, B_ng}, {C_g1, D_g1}};
        verifyFilter(in);

        /* execute */

        // move C to the end of the filter
        f.performDragAndDrop(f.getDraggableItemSelector(3), f.getDropZoneAfterItemSelector(4));
        
        /* verify */

        Item[][] res = { {A_ng, B_ng}, {D_g1, C_ng}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    /**
     * Covering the fix of the issue DE166407 - not able to move elements to the end of BT coverage box (over the [+] button inside)
     * 
     * @throws Exception
     */
    @Test
    public void testMove28MoveItemToTheEndOfBtCoverageAccrossClauses() throws Exception {
        initialize();
        prepareFilters6();

        Item[][] in = { {A_ng, B_ng}, {C_g1, D_g1}};
        verifyFilter(in);

        /* execute */

        // move A to the end of the BT in the second clause
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAtEndOfItemSelector(4));
        
        /* verify */

        Item[][] res = { {B_ng}, {C_g1, D_g1, A_g1}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
    
    /**
     * Covering the fix of the issue DE166407 - not able to move elements to the end of BT coverage box (over the [+] button inside)
     * 
     * @throws Exception
     */
    @Test
    public void testMove29MoveItemToTheEndOfBtCoverageWithinClause() throws Exception {
        initialize();
        prepareFilters7();

        Item[][] in = { {A_ng, B_g1}};
        verifyFilter(in);

        /* execute */

        // move A to the end of the BT 
        f.performDragAndDrop(f.getDraggableItemSelector(1), f.getDropZoneAtEndOfItemSelector(2));
        
        /* verify */

        Item[][] res = { {B_g1, A_g1}};
        verifyFilter(res);

        logger.info("cleanup");
        ui.cleanup();
    }
}
