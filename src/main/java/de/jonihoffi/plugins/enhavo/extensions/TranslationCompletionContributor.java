package de.jonihoffi.plugins.enhavo.extensions;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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
                                    String lookupString = "App\\Entity\\" + file.getNameWithoutExtension();
                                    LookupElementBuilder lookupElement = LookupElementBuilder.create(lookupString)
                                                    .withInsertHandler(new InsertHandler<LookupElement>() {
                                                        @Override
                                                        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
                                                            context.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());

                                                            insertMultilineTextWithRelativeIndentation(context.getEditor(), context.getDocument(), context.getStartOffset(), lookupString);
                                                        }
                                                    });
                                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0));
                                }
                            }
                        }
                        if (baseDirBlock != null && baseDirBlock.isDirectory()) {
                            for (VirtualFile file : baseDirBlock.getChildren()) {
                                if ("php".equals(file.getExtension())) {
                                    String lookupString = "App\\Entity\\Block\\" + file.getNameWithoutExtension();
                                    LookupElementBuilder lookupElement = LookupElementBuilder.create(lookupString)
                                            .withInsertHandler(new InsertHandler<LookupElement>() {
                                                @Override
                                                public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
                                                    context.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());

                                                    insertMultilineTextWithRelativeIndentation(context.getEditor(), context.getDocument(), context.getStartOffset(), lookupString);
                                                }
                                            });
                                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0));
                                }
                            }
                        }
                    }


                    private void insertMultilineTextWithRelativeIndentation(Editor editor, Document document, int startOffset, String lookupString) {
                        String currentIndentation = getCurrentLineIndentation(editor, startOffset);

                        String firstLine = lookupString + ":";
                        String secondLine = "   properties:";
                        String multilineText = firstLine + "\n" + currentIndentation + secondLine;
                        document.insertString(startOffset, multilineText);
                        editor.getCaretModel().moveToOffset(startOffset + multilineText.length());
                    }

                    private String getCurrentLineIndentation(Editor editor, int offset) {
                        Document document = editor.getDocument();
                        int lineNumber = document.getLineNumber(offset);
                        int lineStartOffset = document.getLineStartOffset(lineNumber);
                        int whitespaceEndOffset = findWhitespaceEndOffset(document.getCharsSequence(), lineStartOffset);
                        return document.getCharsSequence().subSequence(lineStartOffset, whitespaceEndOffset).toString();
                    }

                    private int findWhitespaceEndOffset(CharSequence text, int startOffset) {
                        int offset = startOffset;
                        while (offset < text.length() && (text.charAt(offset) == ' ' || text.charAt(offset) == '\t')) {
                            offset++;
                        }
                        return offset;
                    }
                }
        );
    }
}

