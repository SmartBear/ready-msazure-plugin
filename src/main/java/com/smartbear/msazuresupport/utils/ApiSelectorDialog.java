package com.smartbear.msazuresupport.utils;

import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.msazuresupport.Strings;
import com.smartbear.msazuresupport.entities.ApiInfo;
import com.smartbear.rapisupport.Service;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ApiSelectorDialog implements AutoCloseable {
    private final List<ApiInfo> apis;
    private final XFormDialog dialog;
    private final JList apiListBox;

    public ApiSelectorDialog(List<ApiInfo> apiList) {
        apis = apiList;

        dialog = ADialogBuilder.buildDialog(SelectAPIFromMsAzureForm.class);
        ListModel<String> listBoxModel = new AbstractListModel<String>() {
            @Override
            public int getSize() {
                return apis.size();
            }

            @Override
            public String getElementAt(int index) {
                return apis.get(index).name;
            }
        };
        apiListBox = new JList(listBoxModel);
        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).setProperty("component", new JScrollPane(apiListBox));
        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).setProperty("preferredSize", new Dimension(500, 150));
        dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setProperty("preferredSize", new Dimension(500, 50));
        dialog.setValue(SelectAPIFromMsAzureForm.DESCRIPTION, null);
        dialog.setValue(SelectAPIFromMsAzureForm.SPEC, null);

        apiListBox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selected = apiListBox.getSelectedIndices();
                if (selected != null && selected.length == 1) {
                    int selectedNo = selected[0];
                    dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setValue(apis.get(selectedNo).description);
                    dialog.getFormField(SelectAPIFromMsAzureForm.SPEC).setValue(apis.get(selectedNo).path);
                } else {
                    dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setValue(null);
                    dialog.getFormField(SelectAPIFromMsAzureForm.SPEC).setValue(null);
                }
            }
        });
        apiListBox.setSelectedIndex(-1);

        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField formField) {
                int[] selected = apiListBox.getSelectedIndices();
                if (selected == null || selected.length == 0) {
                    return new ValidationMessage[]{new ValidationMessage(Strings.SelectApiDialog.NOTHING_SELECTED_WARNING, formField)};
                } else {
                    return new ValidationMessage[0];
                }
            }
        });

        final XFormField checkLoadTest = dialog.getFormField(SelectAPIFromMsAzureForm.LOAD_TEST);
        final XFormField checkSecurTest = dialog.getFormField(SelectAPIFromMsAzureForm.SECUR_TEST);
        checkLoadTest.setEnabled(false);
        checkSecurTest.setEnabled(false);
        dialog.getFormField(SelectAPIFromMsAzureForm.TEST_SUITE).addFormFieldListener(new XFormFieldListener() {
            @Override
            public void valueChanged(XFormField xFormField, String s, String s1) {
                boolean enabled = dialog.getBooleanValue(SelectAPIFromMsAzureForm.TEST_SUITE);
                checkLoadTest.setEnabled(enabled);
                checkSecurTest.setEnabled(enabled);
                if (!enabled) {
                    checkLoadTest.setValue("false");
                    checkSecurTest.setValue("false");
                }
            }
        });
    }

    public Result getSelectedApi() {
        return dialog.show() ? new Result() : null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    public class Result {
        public final List<ApiInfo> selectedAPIs = new ArrayList<>();
        public final Set<Service> entities = EnumSet.noneOf(Service.class);

        public Result() {
            int[] selected = apiListBox.getSelectedIndices();
            for (int index : selected) {
                selectedAPIs.add(apis.get(index));
            }

            if (dialog.getBooleanValue(SelectAPIFromMsAzureForm.TEST_SUITE)) {
                entities.add(Service.TEST_SUITE);
            }
            if (dialog.getBooleanValue(SelectAPIFromMsAzureForm.LOAD_TEST)) {
                entities.add(Service.LOAD_TEST);
            }
            if (dialog.getBooleanValue(SelectAPIFromMsAzureForm.VIRT)) {
                entities.add(Service.VIRT);
            }
            if (dialog.getBooleanValue(SelectAPIFromMsAzureForm.SECUR_TEST)) {
                entities.add(Service.SECUR_TEST);
            }
        }
    }

    @AForm(name = Strings.SelectApiDialog.CAPTION, description = Strings.SelectApiDialog.DESCRIPTION)
    public interface SelectAPIFromMsAzureForm {
        @AField(description = Strings.SelectApiDialog.NAME_LABEL, type = AField.AFieldType.COMPONENT)
        public final static String NAME = "Name";

        @AField(description = Strings.SelectApiDialog.DESCRIPTION_LABEL, type = AField.AFieldType.INFORMATION)
        public final static String DESCRIPTION = "Description";

        @AField(description = Strings.SelectApiDialog.DEFINITION_LABEL, type = AField.AFieldType.LABEL)
        public final static String SPEC = "Definition";

        @AField(description = "", type = AField.AFieldType.SEPARATOR)
        public final static String SEPERATOR = "Separator";

        @AField(name = "###GenerateTestSuite", description = Strings.SelectApiDialog.GEN_TEST_SUITE, type = AField.AFieldType.BOOLEAN)
        public final static String TEST_SUITE = "###GenerateTestSuite";

        @AField(name = "###GenerateLoadTest", description = Strings.SelectApiDialog.GEN_LOAD_TEST, type = AField.AFieldType.BOOLEAN)
        public final static String LOAD_TEST = "###GenerateLoadTest";

        @AField(name = "###GenerateSecurTest", description = Strings.SelectApiDialog.GEN_SECUR_TEST, type = AField.AFieldType.BOOLEAN)
        public final static String SECUR_TEST = "###GenerateSecurTest";

        @AField(name = "###GenerateVirt", description = Strings.SelectApiDialog.GEN_VIRT_HOST, type = AField.AFieldType.BOOLEAN)
        public final static String VIRT = "###GenerateVirt";
    }
}
