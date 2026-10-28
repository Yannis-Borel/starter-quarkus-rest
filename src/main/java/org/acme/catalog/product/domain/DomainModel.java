package org.acme.catalog.product.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un modèle du domaine. Traitée <b>à la compilation</b> par
 * {@code org.acme.processor.DomainModelProcessor}, qui :
 * <ol>
 * <li>refuse de compiler si la classe annotée n'est pas un {@code record}
 * (le domaine doit être immuable) ;</li>
 * <li>génère une classe {@code XxxFields} avec une constante {@code String}
 * par composant du record (par exemple {@code UserFields.USERNAME}).</li>
 * </ol>
 *
 * <p>
 * Notion de cours : processeur d'annotations (cours Annotations 2/2).
 * {@code @Retention(SOURCE)} : seul le compilateur en a besoin, elle n'est
 * même pas écrite dans le {@code .class}. C'est l'opposé de
 * {@code @ValidUsername}, lue par introspection à l'exécution et donc en
 * {@code RUNTIME}. {@code @Target(TYPE)} : elle se pose sur une classe, un
 * record, une interface ou un enum (le processeur n'accepte ensuite que les
 * records).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DomainModel {
}
