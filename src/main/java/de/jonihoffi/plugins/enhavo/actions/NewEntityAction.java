package de.jonihoffi.plugins.enhavo.actions;

import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import de.jonihoffi.plugins.enhavo.dialogs.NewBlockYamlFileDialog;
import de.jonihoffi.plugins.enhavo.dialogs.NewEntityDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class NewEntityAction extends AnAction {
    public NewEntityAction() {
        super("New Entity", "Creates a new entity file", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        if (view == null) return;

        PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) return;

        NewEntityDialog dialog = new NewEntityDialog();
        if (dialog.showAndGet()) {
            String name = dialog.getName();
            if (!name.isEmpty()) {
                String nameWithoutExtension = name.endsWith(".php") ? name.substring(0, name.length() - 4) : name;
                createEntityFile(project, directory, nameWithoutExtension, dialog);
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
            if (isEntityDirectory(dir.getVirtualFile())) {
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

    private boolean isEntityDirectory(VirtualFile directory) {
        return directory.isDirectory() && "Entity".equals(directory.getName());
    }

    private void createEntityFile(Project project, PsiDirectory directory, String name, NewEntityDialog dialog) {
        try {
            String repositoryName = "test";
            String tableName = dialog.getTable();
            Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
            properties.setProperty("NAME", name);
            properties.setProperty("REPOSITORY_NAME", repositoryName);
            properties.setProperty("TABLE_NAME", tableName);

            var template = FileTemplateManager.getInstance(project).getInternalTemplate("PHP Entity File Template");
            PsiFile file = FileTemplateUtil.createFromTemplate(template, name + ".php", properties, directory).getContainingFile();
            openFileInEditor(project, file.getVirtualFile());
        } catch (Exception e) {
            Messages.showErrorDialog(project, "Error creating file: " + e.getMessage(), "Error");
        }
    }

    private void openFileInEditor(Project project, VirtualFile virtualFile) {
        FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }
}
