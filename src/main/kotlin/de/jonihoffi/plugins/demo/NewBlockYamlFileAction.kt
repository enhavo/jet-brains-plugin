package de.jonihoffi.plugins.demo

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.ui.components.dialog
import de.jonihoffi.plugins.demo.dialog.NewBlockYamlFileDialog

class NewBlockYamlFileAction : AnAction("New enhavo-block yaml file", "Creates a new enhavo-block yaml file", null) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val view = event.getData(LangDataKeys.IDE_VIEW) ?: return
        val directory = view.orChooseDirectory ?: return

        val dialog = NewBlockYamlFileDialog()
        if (dialog.showAndGet()) {
            val name = dialog.getName()
            if (name.isNotEmpty()) {
                val nameWithoutExtension = if (name.endsWith(".yaml")) name.removeSuffix(".yaml") else name
                createBlockYamlFile(project, directory, nameWithoutExtension, dialog)
            } else {
                Messages.showErrorDialog(project, "The file name cannot be empty.", "Error")
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val project = event.project
        val view = event.getData(LangDataKeys.IDE_VIEW)
        val directories = view?.directories ?: return

        event.presentation.isEnabledAndVisible = directories.any { dir ->
            isBlockDirectory(dir.virtualFile)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun isBlockDirectory(file: VirtualFile): Boolean {
        return file.isDirectory && (file.name == "block" || file.parent?.name == "block")
    }

    private fun createBlockYamlFile(project: Project, directory: PsiDirectory, name: String, dialog: NewBlockYamlFileDialog) {
        try {
            val templateName = if (dialog.includeItems()) "Block Item YAML File Template" else "Block YAML File Template"
            val properties = FileTemplateManager.getInstance(project).defaultProperties
            properties["NAME"] = name
            properties["CAMELCASE"] = lowerCaseFirstChar(name + "Block")
            properties["GROUPS"] = dialog.getGroups()

            val template = FileTemplateManager.getInstance(project).getInternalTemplate(templateName)
            val file: PsiFile = FileTemplateUtil.createFromTemplate(template, name + "Block.yaml", properties, directory) as PsiFile
            openFileInEditor(project, file.virtualFile)
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Error creating file: ${e.message}", "Error")
        }
    }

    private fun openFileInEditor(project: Project, virtualFile: VirtualFile) {
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }

    private fun lowerCaseFirstChar(input: String?): String? {
        if (input.isNullOrEmpty()) {
            return input
        }
        val firstChar = input[0].lowercaseChar()
        if (input.length == 1) {
            return firstChar.toString()
        }
        return firstChar.toString() + input.substring(1)
    }
}