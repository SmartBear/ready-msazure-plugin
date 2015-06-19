package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.msazuresupport.Strings;

import java.net.URL;

public class AddApiDialog implements AutoCloseable {

    public static class Result {
        public final String portalUrl;
        public final String accessToken;

        public Result(String portalUrl, String accessToken) {
            this.portalUrl = portalUrl;
            this.accessToken = accessToken;
        }
    }

    private final XFormDialog dialog;
    private final XFormField portalUrlField;
    private final XFormField accessTokenField;

    public AddApiDialog() {
        dialog = ADialogBuilder.buildDialog(Form.class);

        portalUrlField = dialog.getFormField(Form.DEVELOPER_PORTAL_URL);
        accessTokenField = dialog.getFormField(Form.ACCESS_TOKEN);

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

                return new ValidationMessage[0];
            }
        });

        accessTokenField.addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField formField) {
                if (StringUtils.isNullOrEmpty(formField.getValue())) {
                    return new ValidationMessage[]{new ValidationMessage(Strings.NewProjectDialog.EMPTY_ACCESS_TOKEN_WARNING, formField)};
                }
                return new ValidationMessage[0];
            }
        });
    }

    public Result show() {
        return dialog.show() ?
                new Result(AzureApi.stringToUrl(portalUrlField.getValue()).toString(), accessTokenField.getValue()) :
                null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    @AForm(name = Strings.AddApiAction.PROMPT_API_DIALOG_CAPTION, description = Strings.AddApiAction.PROMPT_API_DIALOG_DESCRIPTION)
    public interface Form {
        @AField(name = Strings.NewProjectDialog.URL_LABEL, description = Strings.NewProjectDialog.URL_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String DEVELOPER_PORTAL_URL = Strings.NewProjectDialog.URL_LABEL;

        @AField(description = Strings.NewProjectDialog.CREDENTIALS_LABEL, type = AField.AFieldType.SEPARATOR)
        public final static String CREDENTIALS = Strings.NewProjectDialog.CREDENTIALS_LABEL;

        @AField(name = Strings.NewProjectDialog.ACCESS_TOKEN_LABEL, description = Strings.NewProjectDialog.ACCESS_TOKEN_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String ACCESS_TOKEN = Strings.NewProjectDialog.ACCESS_TOKEN_LABEL;
    }
}
