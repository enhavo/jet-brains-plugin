package de.jonihoffi.plugins.enhavo.extensions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.jonihoffi.plugins.enhavo.BlockToolWindow;
import de.jonihoffi.plugins.enhavo.MigrationToolWindow;
import org.jetbrains.annotations.NotNull;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationToolWindow migrationToolWindow = new MigrationToolWindow(project);
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(migrationToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}