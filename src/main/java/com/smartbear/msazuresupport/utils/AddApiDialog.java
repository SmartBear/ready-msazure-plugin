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
import com.smartbear.msazuresupport.entities.ApiInfo;

import java.net.URL;
import java.util.List;

public class AddApiDialog implements AutoCloseable {

    public static class Result {
        public final String portalUrl;
        public final String accessToken;
        public final List<ApiInfo> apis;

        public Result(String portalUrl, String accessToken, List<ApiInfo> apis) {
            this.portalUrl = portalUrl;
            this.accessToken = accessToken;
            this.apis = apis;
        }
    }

    private final XFormDialog dialog;
    private final XFormField portalUrlField;
    private final XFormField accessTokenField;
    private ApiListLoader.Result loaderResult = null;

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

                ValidationMessage[] msg = downloadApiList();
                if (msg.length > 0) {
                    return msg;
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

                ValidationMessage[] msg = downloadApiList();
                if (msg.length > 0) {
                    return msg;
                }

                return new ValidationMessage[0];
            }
        });
    }

    public Result show() {
        return dialog.show() ?
                new Result(AzureApi.stringToUrl(portalUrlField.getValue()).toString(), accessTokenField.getValue(), loaderResult.apis) :
                null;
    }

    private ValidationMessage[] downloadApiList() {
        String portalUrl = portalUrlField.getValue();
        String accessToken = accessTokenField.getValue();
        if (!StringUtils.isNullOrEmpty(portalUrl) && !StringUtils.isNullOrEmpty(accessToken)) {
            loaderResult = ApiListLoader.downloadList(new AzureApi.ConnectionSettings(portalUrl, accessToken));
            if (loaderResult.authorizationFailed) {
                return new ValidationMessage[]{new ValidationMessage(Strings.NewProjectDialog.INVALID_ACCESS_TOKEN_WARNING, accessTokenField)};
            } else if (StringUtils.hasContent(loaderResult.error)) {
                return new ValidationMessage[]{new ValidationMessage(loaderResult.error, portalUrlField)};
            }
        }
        return new ValidationMessage[0];
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
