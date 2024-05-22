package de.jonihoffi.plugins.enhavo.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField myTemplatePath = new JBTextField("/config/block/templates");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter user name: "), myTemplatePath, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myTemplatePath;
    }

    @NotNull
    public String getTemplatePathText() {
        return myTemplatePath.getText();
    }

    public void setTemplatePathText(@NotNull String newText) {
        myTemplatePath.setText(newText);
    }
}