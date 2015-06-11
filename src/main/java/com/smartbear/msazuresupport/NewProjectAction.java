package com.smartbear.msazuresupport;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.auto.PluginImportMethod;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.msazuresupport.utils.ApiImporter;
import com.smartbear.msazuresupport.utils.ApiSelectorDialog;
import com.smartbear.msazuresupport.utils.NewProjectDialog;
import com.smartbear.msazuresupport.utils.SubscriptionKeyInputDialog;
import com.smartbear.rapisupport.ServiceFactory;

import java.util.List;

@PluginImportMethod(label = Strings.NewProjectAction.ACTION_CAPTION)
public class NewProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {

    public NewProjectAction() {
        super(Strings.NewProjectAction.NAME, Strings.NewProjectAction.DESCRIPTION);
    }

    @Override
    public void perform(final WorkspaceImpl target, Object param) {
        NewProjectDialog.Result result = null;
        try (NewProjectDialog dialog = new NewProjectDialog()) {
            result = dialog.show();
        }

        if (result == null) {
            return;
        }

        ApiSelectorDialog.Result selResult = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(result.apis)) {
            selResult = dlg.getSelectedApi();
        }

        if (selResult == null) {
            return;
        }

        try (SubscriptionKeyInputDialog dlg = new SubscriptionKeyInputDialog(selResult.selectedAPIs)) {
            if (!dlg.show()) {
                return;
            }
        }

        WsdlProject project;
        try {
            project = target.createProject(result.projectName, null);
        } catch (Exception e) {
            SoapUI.logError(e);
            UISupport.showErrorMessage(String.format(Strings.NewProjectAction.UNABLE_CREATE_ERROR, e.getClass().getName(), e.getMessage()));
            return;
        }

        if (project == null) {
            return;
        }

        List<RestService> services = ApiImporter.importServices(result.portalUrl, selResult.selectedAPIs, project);
        ServiceFactory.Build(project, services, selResult.entities);

        if (services.size() > 0) {
            UISupport.select(services.get(0));
        } else {
            target.removeProject(project);
        }
    }
}
