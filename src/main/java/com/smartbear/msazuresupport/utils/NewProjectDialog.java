package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NewProjectDialog implements AutoCloseable {
    public static class Result {
        public final String projectName;
        public final String portalUrl;
        public final List<AzureApi.ApiInfo> apis;

        public Result(String projectName, String portalUrl, List<AzureApi.ApiInfo> apis) {
            this.projectName = projectName;
            this.portalUrl = portalUrl;
            this.apis = new ArrayList<>(apis);
        }
    }

    private XFormDialog dialog;
    private final XFormField portalUrlField;
    private final XFormField projectNameField;
    private ApiListLoader.Result loaderResult = null;

    public NewProjectDialog() {
        dialog = ADialogBuilder.buildDialog(Form.class);
        portalUrlField = dialog.getFormField(Form.DEVELOPER_PORTAL_URL);
        projectNameField = dialog.getFormField(Form.PROJECT_NAME);

        portalUrlField.addFormFieldListener(new XFormFieldListener() {
            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                if (StringUtils.isNullOrEmpty(projectNameField.getValue())) {
                    String defProjectName = getDefaultProjectName(newValue);
                    dialog.setValue(Form.PROJECT_NAME, defProjectName);
                }
            }
        });
        portalUrlField.addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField formField) {
                if (StringUtils.isNullOrEmpty(formField.getValue())) {
                    return new ValidationMessage[]{new ValidationMessage("Please enter the developer portal URL.", formField)};
                }

                URL portalUrl = AzureApi.stringToUrl(formField.getValue());
                if (portalUrl == null) {
                    return new ValidationMessage[]{new ValidationMessage("Invalid developer portal URL.", formField)};
                }
                loaderResult = ApiListLoader.downloadList(portalUrl);
                if (StringUtils.hasContent(loaderResult.error)) {
                    return new ValidationMessage[]{new ValidationMessage(loaderResult.error, formField)};
                }
                return new ValidationMessage[0];
            }
        });

        projectNameField.addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField formField) {
                if (StringUtils.isNullOrEmpty(formField.getValue())) {
                    return new ValidationMessage[]{new ValidationMessage("Please enter project name.", formField)};
                }
                return new ValidationMessage[0];
            }
        });
    }

    public Result show() {
        return dialog.show() && !loaderResult.canceled ?
                new Result(projectNameField.getValue(), portalUrlField.getValue(), loaderResult.apis) :
                null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    private String getDefaultProjectName(String newValue) {
        if (!StringUtils.hasContent(newValue)) {
            return "";
        }

        newValue = newValue.trim();

        for (String prefix : new String[]{"http://", "https://"}) {
            if (newValue.toLowerCase().startsWith(prefix)) {
                newValue = newValue.substring(prefix.length());
                break;
            }
        }

        if (newValue.toLowerCase().endsWith(".management.azure-api.net")) {
            newValue = newValue.substring(0, newValue.length() - ".management.azure-api.net".length());
        }

        return newValue;
    }

    @AForm(name = "Create Project From API Specification on MS Azure Portal", description = "Creates a new Project from API specification on MS Azure developer portal in this workspace")
    private interface Form {
        @AField(name = "Project Name", description = "Name of the project", type = AField.AFieldType.STRING)
        public final static String PROJECT_NAME = "Project Name";

        @AField(name = "Developer Portal URL", description = "Developer portal URL (i.e. developer.management.azure-api.net)", type = AField.AFieldType.STRING)
        public final static String DEVELOPER_PORTAL_URL = "Developer Portal URL";
    }
}
