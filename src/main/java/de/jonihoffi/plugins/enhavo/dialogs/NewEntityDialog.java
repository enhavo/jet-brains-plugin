package de.jonihoffi.plugins.enhavo.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NewEntityDialog extends DialogWrapper {

    private final JBTextField nameField = new JBTextField();
    private final JBTextField tableField = new JBTextField("app_");

    public NewEntityDialog() {
        super(true);
        init();
        setTitle("New Entity");
        setSize(500, 100);
    }

    public String getName() {
        return nameField.getText();
    }

    public String getTable() {
        return tableField.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameField)
                .addLabeledComponent("Table:", tableField)
                .getPanel();
    }
}
