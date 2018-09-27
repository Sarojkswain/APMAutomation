/*
 * Copyright (c) 2015 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.test.atc.common;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class ModalDialog {

    private static final String MODAL_DIALOG_CSS_SELECTOR = "div.modal-dialog";

    public static enum DialogButton {
        CONTINUE(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button#btn-continue")),
        YES(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonYes\"]")),
        NO(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonNo\"]")),
        SAVE(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonSave\"]")),
        CLOSE(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button.close")),
        SAVE_UNIVERSE(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonSave\"]")),
        SAVE_AS_UNIVERSE(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonSaveAs\"]")),
        CONTINUE_EDITING(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR + " button[name=\"buttonCancel\"]"));

        private final By selector;

        private DialogButton(By selector) {
            this.selector = selector;
        }

        public By getSelector() {
            return selector;
        }
    };

    private final UI ui;

    public ModalDialog(UI ui) {
        this.ui = ui;
    }

    /**
     * Return element of modal dialog
     * @throws Exception
     */
    public PageElement getModalDialog() throws Exception {
      waitForModalDialogFadeIn();
      return ui.getElementProxy(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR));
    }

    public boolean isModalDialogPresent() {
        return ui.findElements(By.cssSelector(MODAL_DIALOG_CSS_SELECTOR)).size() > 0;
      }

    /**
     * Wait until Modal dialog disappears
     */
    public void waitForModalDialogFadeOut() {
        ui.waitWhileVisible(By.cssSelector("[modal-render='true']"));
        // animation duration
        Utils.sleep(300);
    }

    /**
     * Wait until Modal dialog fully appears
     */
    public void waitForModalDialogFadeIn() throws Exception {
        ui.waitUntilVisible(By.cssSelector("[modal-render='true']"));
    }

    public PageElement getRadioOption(int index) throws Exception {
      return this.getModalDialog().findElement(By.cssSelector("input[type='radio'][id='dialogSelectionOption" + index + "']"
      ));
    }

    /**
     * Return element of modal dialog button
     * @throws Exception
     */
    public PageElement getButton(DialogButton button) throws Exception {
      return ui.getElementProxy(button.getSelector());
    }

    public void clickButton(DialogButton button) throws Exception {
        getButton(button).click();
        waitForModalDialogFadeOut();
    }

    public PageElement findBySelector(By selector) throws Exception {
        return this.getModalDialog().findElement(selector);
    }
}
