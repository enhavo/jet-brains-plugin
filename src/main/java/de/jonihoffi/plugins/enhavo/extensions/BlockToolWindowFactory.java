package de.jonihoffi.plugins.enhavo.extensions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.jonihoffi.plugins.enhavo.BlockToolWindow;
import org.jetbrains.annotations.NotNull;

public class BlockToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BlockToolWindow blockToolWindow = new BlockToolWindow(project);
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(blockToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}