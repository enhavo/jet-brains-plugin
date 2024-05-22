package de.jonihoffi.plugins.demo

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.YAMLLanguage

class TemplateCompletionContributor : CompletionContributor() {
    init {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE)
                        .and(PlatformPatterns.psiElement().inside(YAMLKeyValue::class.java))
                        .and(PlatformPatterns.psiElement().withSuperParent(2, PlatformPatterns.psiElement(YAMLKeyValue::class.java)
                                .withName(StandardPatterns.string().equalTo("template")))),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(
                            parameters: CompletionParameters,
                            context: ProcessingContext,
                            resultSet: CompletionResultSet
                    ) {
                        val project = parameters.position.project
                        val templateDir = project.baseDir.findFileByRelativePath("config/block/templates")
                        if (templateDir != null && templateDir.isDirectory) {
                            templateDir.children.filter { it.extension == "yaml" }
                                    .forEach {
                                        val lookupElement = LookupElementBuilder.create(it.nameWithoutExtension)
                                        val prioritizedElement = PrioritizedLookupElement.withPriority(lookupElement, 100.0)
                                        resultSet.addElement(prioritizedElement)
                                    }
                        }
                    }
                }
        )
    }
}
