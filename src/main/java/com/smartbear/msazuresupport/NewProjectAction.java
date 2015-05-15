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
import com.smartbear.msazuresupport.utils.AzureApi;
import com.smartbear.msazuresupport.utils.NewProjectDialog;

import java.util.List;

@PluginImportMethod(label = "MS Azure Developer Portal (REST)")
public class NewProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {

    public NewProjectAction() {
        super("Create Project From MS Azure", "Creates a new project from API specification on MS Azure developer portal.");
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
        List<AzureApi.ApiInfo> selectedAPIs = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(result.apis)) {
            selectedAPIs = dlg.getSelectedApi();
        }

        if (selectedAPIs == null) {
            return;
        }

        WsdlProject project;
        try {
            project = target.createProject(result.projectName, null);
        } catch (Exception e) {
            SoapUI.logError(e);
            UISupport.showErrorMessage(String.format("Unable to create Project because of %s exception with \"%s\" message", e.getClass().getName(), e.getMessage()));
            return;
        }

        if (project == null) {
            return;
        }

        List<RestService> services = ApiImporter.importServices(result.portalUrl, selectedAPIs, project);
        if (services.size() > 0) {
            UISupport.select(services.get(0));
        } else {
            target.removeProject(project);
        }
    }
}
