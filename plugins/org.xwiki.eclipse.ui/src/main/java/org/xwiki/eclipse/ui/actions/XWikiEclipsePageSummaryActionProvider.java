/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.eclipse.ui.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.xwiki.eclipse.core.CoreLog;
import org.xwiki.eclipse.model.XWikiEclipsePage;
import org.xwiki.eclipse.model.XWikiEclipsePageHistorySummary;
import org.xwiki.eclipse.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.model.XWikiEclipsePageTranslationSummary;
import org.xwiki.eclipse.storage.XWikiEclipseStorageException;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.PageEditorInput;
import org.xwiki.eclipse.ui.utils.UIUtils;

/**
 * @version $Id$
 */
public class XWikiEclipsePageSummaryActionProvider
{
    private Action open;

    private CommandContributionItem newObject;

    private CommandContributionItem newComment;

    private CommandContributionItem newTag;

    private CommandContributionItem uploadAttachment;

    private CommandContributionItem renamePage;

    private CommandContributionItem copyPage;

    private ISelectionProvider selectionProvider;

    public XWikiEclipsePageSummaryActionProvider(ISelectionProvider selectionProvider)
    {
        this.selectionProvider = selectionProvider;

        open = new OpenXWikiModelObjectAction(selectionProvider);

        CommandContributionItemParameter ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.NEW_OBJECT_COMMAND, UIConstants.NEW_OBJECT_COMMAND, SWT.NONE);

        newObject = new CommandContributionItem(ccip);

        ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.NEW_COMMENT_COMMAND, UIConstants.NEW_COMMENT_COMMAND, SWT.NONE);

        newComment = new CommandContributionItem(ccip);

        ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.UPLOAD_ATTACHMENT_COMMAND, UIConstants.UPLOAD_ATTACHMENT_COMMAND, SWT.NONE);

        uploadAttachment = new CommandContributionItem(ccip);

        ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.NEW_TAG_COMMAND, UIConstants.NEW_TAG_COMMAND, SWT.NONE);

        newTag = new CommandContributionItem(ccip);

        ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.RENAME_PAGE_COMMAND, UIConstants.RENAME_PAGE_COMMAND, SWT.NONE);

        renamePage = new CommandContributionItem(ccip);

        ccip =
            new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                UIConstants.COPY_PAGE_COMMAND, UIConstants.COPY_PAGE_COMMAND, SWT.NONE);

        copyPage = new CommandContributionItem(ccip);
    }

    public void fillContextMenu(IMenuManager menu)
    {
        menu.add(open);

        menu.add(getTranslationMenu());
        menu.add(getHistoryMenu());

        menu.add(new Separator());
        menu.add(newObject);
        menu.add(newComment);
        menu.add(newTag);
        menu.add(uploadAttachment);
        menu.add(new Separator());
        menu.add(renamePage);
        menu.add(copyPage);
    }

    private IMenuManager getHistoryMenu()
    {
        MenuManager menuManager = new MenuManager("Open version...");

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selectionProvider.getSelection());
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();

            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                try {
                    List<XWikiEclipsePageHistorySummary> pageHistory =
                        pageSummary.getDataManager().getPageHistories(pageSummary.getWiki(), pageSummary.getSpace(),
                            pageSummary.getName(), pageSummary.getLanguage());
                    for (XWikiEclipsePageHistorySummary pageHistorySummary : pageHistory) {
                        menuManager.add(new OpenPageHistoryItemAction(pageHistorySummary));
                    }
                } catch (XWikiEclipseStorageException e) {
                    CoreLog.logError("Unable to build history menu", e);
                }
            }
        }

        return menuManager;
    }

    private IMenuManager getTranslationMenu()
    {
        MenuManager menuManager = new MenuManager("Open translation...");

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selectionProvider.getSelection());
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();

            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                try {
                    XWikiEclipsePageSummary updatedPageSummary =
                        pageSummary.getDataManager().getPageSummary(pageSummary.getWiki(), pageSummary.getSpace(),
                            pageSummary.getName(), null);

                    if (updatedPageSummary.getTranslations() != null) {
                        for (XWikiEclipsePageTranslationSummary translation : updatedPageSummary.getTranslations()) {
                            if (!translation.getLanguage().equals(translation.getDefaultLanguage())) {
                                menuManager.add(new OpenPageTranslationAction(updatedPageSummary, translation));
                            }
                        }
                    }
                } catch (XWikiEclipseStorageException e) {
                    CoreLog.logError("Unable to build translations menu", e);
                }

                /* TODO: Checkout the server side. This gives non-deterministic results */
                menuManager.add(new Separator());

                menuManager.add(new Action("New translation...")
                {
                    @Override
                    public void run()
                    {
                        InputDialog inputDialog =
                            new InputDialog(Display.getDefault().getActiveShell(), "Translation", "Translation", "",
                                null);
                        inputDialog.open();

                        if (inputDialog.getReturnCode() == InputDialog.OK) {
                            if (!inputDialog.getValue().equals("")) {
                                try {
                                    XWikiEclipsePage page =
                                        pageSummary.getDataManager().createPage(pageSummary.getWiki(),
                                            pageSummary.getSpace(), pageSummary.getName(), pageSummary.getTitle(),
                                            inputDialog.getValue(), "Write translation here");
                                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                        .openEditor(new PageEditorInput(page, false), PageEditor.ID);
                                } catch (Exception e) {
                                    CoreLog.logError(String.format("Error running action %s", getId()), e);
                                }
                            }
                        }
                    }
                });
            }
        }

        return menuManager;
    }
}
