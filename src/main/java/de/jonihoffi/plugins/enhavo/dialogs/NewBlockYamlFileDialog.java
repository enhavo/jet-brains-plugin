package de.jonihoffi.plugins.enhavo.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class NewBlockYamlFileDialog extends DialogWrapper {

    private final JBTextField nameField = new JBTextField();
    private final JBTextField groupsField = new JBTextField("layout");
    private final JBCheckBox includeItemsCheckBox = new JBCheckBox("Block has items");

    public NewBlockYamlFileDialog() {
        super(true);
        init();
        setTitle("New Enhavo Block");
        setSize(500, 100);
    }

    public String getName() {
        return nameField.getText();
    }

    public boolean includeItems() {
        return includeItemsCheckBox.isSelected();
    }

    public String getGroups() {
        return groupsField.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameField)
                .addLabeledComponent("Groups: (Separate with comma)", groupsField)
                .addComponent(includeItemsCheckBox)
                .getPanel();
    }
}
