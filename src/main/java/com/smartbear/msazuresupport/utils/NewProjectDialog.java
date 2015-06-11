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
import com.smartbear.msazuresupport.Strings;

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
                    return new ValidationMessage[]{new ValidationMessage(Strings.NewProjectDialog.EMPTY_URL_WARNING, formField)};
                }

                URL portalUrl = AzureApi.stringToUrl(formField.getValue());
                if (portalUrl == null) {
                    return new ValidationMessage[]{new ValidationMessage(Strings.NewProjectDialog.INVALID_URL_WARNING, formField)};
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
                    return new ValidationMessage[]{new ValidationMessage(Strings.NewProjectDialog.EMPTY_PROJECT_WARNING, formField)};
                }
                return new ValidationMessage[0];
            }
        });
    }

    public Result show() {
        return dialog.show() && !loaderResult.canceled ?
                new Result(projectNameField.getValue(), AzureApi.stringToUrl(portalUrlField.getValue()).toString(), loaderResult.apis) :
                null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    private static final String URL_TAIL = ".management.azure-api.net";

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

        if (newValue.toLowerCase().endsWith(URL_TAIL)) {
            newValue = newValue.substring(0, newValue.length() - URL_TAIL.length());
        }

        return newValue;
    }

    @AForm(name = Strings.NewProjectDialog.CAPTION, description = Strings.NewProjectDialog.DESCRIPTION)
    private interface Form {
        @AField(name = Strings.NewProjectDialog.PROJECT_LABEL, description = Strings.NewProjectDialog.PROJECT_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String PROJECT_NAME = Strings.NewProjectDialog.PROJECT_LABEL;

        @AField(name = Strings.NewProjectDialog.URL_LABEL, description = Strings.NewProjectDialog.URL_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String DEVELOPER_PORTAL_URL = Strings.NewProjectDialog.URL_LABEL;
    }
}
