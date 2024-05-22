package de.jonihoffi.plugins.enhavo;

import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import de.jonihoffi.plugins.enhavo.dialog.NewBlockYamlFileDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class NewBlockYamlFileAction extends AnAction {
    public NewBlockYamlFileAction() {
        super("New Enhavo Block", "Creates a new enhavo-block yaml file", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        if (view == null) return;

        PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) return;

        NewBlockYamlFileDialog dialog = new NewBlockYamlFileDialog();
        if (dialog.showAndGet()) {
            String name = dialog.getName();
            if (!name.isEmpty()) {
                String nameWithoutExtension = name.endsWith(".yaml") ? name.substring(0, name.length() - 5) : name;
                createBlockYamlFile(project, directory, nameWithoutExtension, dialog);
            } else {
                Messages.showErrorDialog(project, "The file name cannot be empty.", "Error");
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        if (view == null) return;

        PsiDirectory[] directories = view.getDirectories();
        boolean enabled = false;
        for (PsiDirectory dir : directories) {
            if (isBlockDirectory(dir.getVirtualFile())) {
                enabled = true;
                break;
            }
        }

        event.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isBlockDirectory(VirtualFile file) {
        return file.isDirectory() && ("block".equals(file.getName()) || "block".equals(file.getParent().getName()));
    }

    private void createBlockYamlFile(Project project, PsiDirectory directory, String name, NewBlockYamlFileDialog dialog) {
        try {
            String templateName = dialog.includeItems() ? "Block Item YAML File Template" : "Block YAML File Template";
            Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
            properties.setProperty("NAME", name);
            properties.setProperty("CAMELCASE", lowerCaseFirstChar(name + "Block"));
            properties.setProperty("GROUPS", dialog.getGroups());

            var template = FileTemplateManager.getInstance(project).getInternalTemplate(templateName);
            PsiFile file = FileTemplateUtil.createFromTemplate(template, name + "Block.yaml", properties, directory).getContainingFile();
            openFileInEditor(project, file.getVirtualFile());
        } catch (Exception e) {
            Messages.showErrorDialog(project, "Error creating file: " + e.getMessage(), "Error");
        }
    }

    private void openFileInEditor(Project project, VirtualFile virtualFile) {
        FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }

    private String lowerCaseFirstChar(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char firstChar = Character.toLowerCase(input.charAt(0));
        if (input.length() == 1) {
            return String.valueOf(firstChar);
        }
        return firstChar + input.substring(1);
    }
}
