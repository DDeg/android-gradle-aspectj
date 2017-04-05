package com.archinamon.utils

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.compile.JavaCompile
import java.io.File
import java.util.ArrayList

fun getJavaTask(baseVariantData: BaseVariantData<out BaseVariantOutputData>): JavaCompile? {
    if (baseVariantData.javacTask != null) {
        return baseVariantData.javacTask
    } else if (baseVariantData.javaCompilerTask != null) {
        return baseVariantData.javaCompilerTask as JavaCompile
    }
    return null
}

fun getAjSourceAndExcludeFromJavac(project: Project, variantData: BaseVariantData<out BaseVariantOutputData>): FileCollection {
    val javaTask = getJavaTask(variantData)

    val flavors: List<String>? = variantData.variantConfiguration?.productFlavors?.map { flavor -> flavor.name }
    val srcSet = mutableListOf("main", variantData.variantConfiguration!!.buildType!!.name)
    flavors?.let { srcSet.addAll(it) }

    val srcDirs = srcSet.map { "src/$it/aspectj" }
    val aspects: FileCollection = SimpleFileCollection(srcDirs.map { project.file(it) })

    javaTask!!.exclude { treeElem ->
        treeElem.file in aspects.files
    }

    return aspects.filter(File::exists)
}

fun findAjSourcesForVariant(project: Project, variantName: String): ArrayList<File> {
    val possibleDirs: MutableList<File> = mutableListOf()
    if (project.file("src/main/aspectj").exists()) {
        possibleDirs.add(project.file("src/main/aspectj"))
    }
    val types = variantName.split("(?=\\p{Upper})")
    val root = project.file("src").listFiles()

    root.forEach { file ->
        types.forEach { type ->
            if (file.name.contains(type.toLowerCase()) &&
                    file.list().any { it.contains("aspectj") } &&
                    !possibleDirs.contains(file)) {
                possibleDirs.add(File(file, "aspectj"))
            }
        }
    }

    return ArrayList(possibleDirs)
}

fun getVariantDataList(plugin: BasePlugin): List<BaseVariantData<out BaseVariantOutputData>> {
    return plugin.variantManager.variantDataList
}

internal infix fun <E> ArrayList<in E>.shl(elem: E): ArrayList<in E> {
    this.add(elem)
    return this
}

internal infix fun <E> ArrayList<in E>.from(elems: List<E>) {
    this.addAll(elems)
}