package de.jonihoffi.plugins.demo.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class NewBlockYamlFileDialog : DialogWrapper(true) {

    private val nameField = JBTextField()
    private val groupsField = JBTextField("layout")
    private val includeItemsCheckBox = JBCheckBox("Block has items")

    init {
        init()
        title = "New Enhavo Block"
        setSize(500, 100)
    }

    fun getName(): String {
        return nameField.text
    }

    fun includeItems(): Boolean {
        return includeItemsCheckBox.isSelected
    }

    fun getGroups(): String {
        return groupsField.text
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameField)
                .addLabeledComponent("Groups: (Separate with comma)", groupsField)
                .addComponent(includeItemsCheckBox)
                .panel
    }
}