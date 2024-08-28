package de.jonihoffi.plugins.enhavo.extensions;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.jonihoffi.plugins.enhavo.settings.AppSettingsState;

public class TemplateCompletionContributor extends CompletionContributor {
    public TemplateCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE)
                        .and(PlatformPatterns.psiElement().inside(YAMLKeyValue.class))
                        .and(PlatformPatterns.psiElement().withSuperParent(2, PlatformPatterns.psiElement(YAMLKeyValue.class)
                                .withName(StandardPatterns.string().equalTo("template")))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet resultSet) {
                        Project project = parameters.getPosition().getProject();
                        String templateDir = project.getBasePath() + "/" + AppSettingsState.getInstance().templatePath;
                        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(templateDir);
                        if (baseDir != null && baseDir.isDirectory()) {
                            for (VirtualFile file : baseDir.getChildren()) {
                                if ("yaml".equals(file.getExtension())) {
                                    LookupElementBuilder lookupElement = LookupElementBuilder.create(file.getNameWithoutExtension());
                                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0));
                                }
                            }
                        }
                    }
                }
        );
    }
}

