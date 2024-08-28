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

public class TranslationCompletionContributor extends CompletionContributor {
    public TranslationCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE)
                        .and(PlatformPatterns.psiElement().inside(YAMLKeyValue.class))
                        .inside(PlatformPatterns.psiElement().withSuperParent(2, PlatformPatterns.psiElement(YAMLKeyValue.class)
                                .withName(StandardPatterns.string().equalTo("metadata")))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet resultSet) {
                        Project project = parameters.getPosition().getProject();
                        String blockDir = project.getBasePath() + "/" + AppSettingsState.getInstance().entityPath + "/Block";
                        String entityDir = project.getBasePath() + "/" + AppSettingsState.getInstance().entityPath;
                        VirtualFile baseDirEntity = LocalFileSystem.getInstance().findFileByPath(entityDir);
                        VirtualFile baseDirBlock = LocalFileSystem.getInstance().findFileByPath(blockDir);
                        if (baseDirEntity != null && baseDirEntity.isDirectory()) {
                            for (VirtualFile file : baseDirEntity.getChildren()) {
                                if ("php".equals(file.getExtension())) {
                                    LookupElementBuilder lookupElement = LookupElementBuilder.create("App\\Entity\\" + file.getNameWithoutExtension());
                                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0));
                                }
                            }
                        }
                        if (baseDirBlock != null && baseDirBlock.isDirectory()) {
                            for (VirtualFile file : baseDirBlock.getChildren()) {
                                if ("php".equals(file.getExtension())) {
                                    LookupElementBuilder lookupElement = LookupElementBuilder.create("App\\Entity\\Block\\" + file.getNameWithoutExtension());
                                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0));
                                }
                            }
                        }
                    }
                }
        );
    }
}

