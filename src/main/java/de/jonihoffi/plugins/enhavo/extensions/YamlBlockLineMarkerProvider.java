package de.jonihoffi.plugins.enhavo.extensions;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.content.Content;
import de.jonihoffi.plugins.enhavo.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.awt.event.MouseEvent;

public class YamlBlockLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof YAMLKeyValue keyValue)) {
            return null;
        }

        PsiElement keyElement = keyValue.getKey();
        if (keyElement == null) {
            return null;
        }

        String keyText = keyValue.getName();

        if (keyText.startsWith("Block/")) {
            return new LineMarkerInfo<>(
                    keyElement,
                    keyElement.getTextRange(),
                    AllIcons.Actions.Execute,
                    psiElement -> "Execute command", // Tooltip provider
                    new GutterIconNavigationHandler<>() {
                        @Override
                        public void navigate(MouseEvent e, PsiElement elt) {
                            String blockPath = AppSettingsState.getInstance().blockPath;
                            String fileName = element.getContainingFile().getName();
                            executeCommand(blockPath, fileName, elt.getProject());
                        }
                    },
                    GutterIconRenderer.Alignment.CENTER,
                    () -> "Enhavo Block Command" // Accessible name provider
            );
        }

        return null;
    }

    private void executeCommand(String blockPath, String fileName, Project project) {
        try {
            String command = "bin/console make:enhavo:block -op " + blockPath + fileName;
            GeneralCommandLine commandLine = new GeneralCommandLine("sh", "-c", command);
            commandLine.setWorkDirectory(project.getBasePath());

            ProcessHandler processHandler;
            processHandler = new OSProcessHandler(commandLine);

            TerminalExecutionConsole consoleView = new TerminalExecutionConsole(project, processHandler);

            DefaultActionGroup actions = new DefaultActionGroup();
            actions.addAll(consoleView.createConsoleActions());

            RunnerLayoutUi.Factory factory = RunnerLayoutUi.Factory.getInstance(project);
            RunnerLayoutUi layoutUi = factory.create("Enhavo Block Maker", "Enhavo Block Maker Execution", "Enhavo Block Maker Execution", project);

            Content consoleContent = layoutUi.createContent("YamlBlockCommandConsole", consoleView.getComponent(), "Console", AllIcons.Debugger.Console, consoleView.getPreferredFocusableComponent());
            layoutUi.addContent(consoleContent);

            RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, processHandler, consoleView.getComponent(), "Enhavo Block Command");
            RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor);

            processHandler.startNotify();

            processHandler.addProcessListener(new ProcessListener() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    ConsoleViewContentType contentType = outputType == ProcessOutputTypes.STDOUT ? ConsoleViewContentType.NORMAL_OUTPUT : ConsoleViewContentType.ERROR_OUTPUT;
                    consoleView.print(event.getText(), contentType);
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}