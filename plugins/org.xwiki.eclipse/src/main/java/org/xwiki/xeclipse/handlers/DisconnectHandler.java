package org.xwiki.xeclipse.handlers;

import org.codehaus.swizzle.confluence.SwizzleConfluenceException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.xeclipse.model.IXWikiConnection;
import org.xwiki.xeclipse.model.XWikiConnectionException;
import org.xwiki.xeclipse.utils.XWikiEclipseUtil;

public class DisconnectHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Object selectedObject =
            XWikiEclipseUtil.getSingleSelectedObjectInStructuredSelection(selection);

        if (selectedObject instanceof IXWikiConnection) {
            IXWikiConnection xwikiConnection = (IXWikiConnection) selectedObject;
            
            try {
                xwikiConnection.disconnect();
            } catch (XWikiConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            /*
             * Re-set the current selection in order to force the re-evaluation of the expressions
             * associated to the other handlers that might depend on the changed state of the
             * selected object
             */
            HandlerUtil.getActiveSite(event).getSelectionProvider().setSelection(selection);
        }

        return null;
    }
}
