/*
 * This file is part of fabric-permissions-api, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.fabric.api.permissions.v0;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A simple permissions API.
 */
public interface Permissions {

    /**
     * Gets the {@link TriState state} of a {@code permission} for the given source.
     *
     * @param source     the source
     * @param permission the permission
     * @return the state of the permission
     */
    static @NotNull TriState getPermissionValue(@NotNull CommandSource source, @NotNull String permission) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(permission, "permission");
        return PermissionCheckEvent.EVENT.invoker().onPermissionCheck(source, permission);
    }

    /**
     * Performs a permission check, falling back to the {@code defaultValue} if the resultant
     * state is {@link TriState#DEFAULT}.
     *
     * @param source       the source to perform the check for
     * @param permission   the permission to check
     * @param defaultValue the default value to use if nothing has been set
     * @return the result of the permission check
     */
    static boolean check(@NotNull CommandSource source, @NotNull String permission, boolean defaultValue) {
        TriState value = getPermissionValue(source, permission);
        if (value == TriState.DEFAULT) {
            return defaultValue;
        }
        return value == TriState.TRUE;
    }

    /**
     * Performs a permission check, falling back to requiring the {@code defaultRequiredLevel}
     * if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param source               the source to perform the check for
     * @param permission           the permission to check
     * @param defaultRequiredLevel the required permission level to check for as a fallback
     * @return the result of the permission check
     */
    static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequiredLevel) {
        TriState value = getPermissionValue(source, permission);
        if (value == TriState.DEFAULT) {
            return source.hasPermissionLevel(defaultRequiredLevel);
        }
        return value == TriState.TRUE;
    }

    /**
     * Performs a permission check, falling back to {@code false} if the resultant state
     * is {@link TriState#DEFAULT}.
     *
     * @param source     the source to perform the check for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    static boolean check(@NotNull CommandSource source, @NotNull String permission) {
        TriState value = getPermissionValue(source, permission);
        return value == TriState.TRUE;
    }

    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to the {@code defaultValue} if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param permission   the permission to check
     * @param defaultValue the default value to use if nothing has been set
     * @return a predicate that will perform the permission check
     */
    static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, boolean defaultValue) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultValue);
    }

    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to requiring the {@code defaultRequiredLevel} if the resultant state is
     * {@link TriState#DEFAULT}.
     *
     * @param permission           the permission to check
     * @param defaultRequiredLevel the required permission level to check for as a fallback
     * @return a predicate that will perform the permission check
     */
    static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequiredLevel) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultRequiredLevel);
    }

    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to {@code false} if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param permission the permission to check
     * @return a predicate that will perform the permission check
     */
    static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission);
    }

    /**
     * Gets the {@link TriState state} of a {@code permission} for the given entity.
     *
     * @param entity     the entity
     * @param permission the permission
     * @return the state of the permission
     */
    static @NotNull TriState getPermissionValue(@NotNull Entity entity, @NotNull String permission) {
        Objects.requireNonNull(entity, "entity");
        return getPermissionValue(entity.getCommandSource(), permission);
    }

    /**
     * Performs a permission check, falling back to the {@code defaultValue} if the resultant
     * state is {@link TriState#DEFAULT}.
     *
     * @param entity       the entity to perform the check for
     * @param permission   the permission to check
     * @param defaultValue the default value to use if nothing has been set
     * @return the result of the permission check
     */
    static boolean check(@NotNull Entity entity, @NotNull String permission, boolean defaultValue) {
        Objects.requireNonNull(entity, "entity");
        return check(entity.getCommandSource(), permission, defaultValue);
    }

    /**
     * Performs a permission check, falling back to requiring the {@code defaultRequiredLevel}
     * if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param entity               the entity to perform the check for
     * @param permission           the permission to check
     * @param defaultRequiredLevel the required permission level to check for as a fallback
     * @return the result of the permission check
     */
    static boolean check(@NotNull Entity entity, @NotNull String permission, int defaultRequiredLevel) {
        Objects.requireNonNull(entity, "entity");
        return check(entity.getCommandSource(), permission, defaultRequiredLevel);
    }

    /**
     * Performs a permission check, falling back to {@code false} if the resultant state
     * is {@link TriState#DEFAULT}.
     *
     * @param entity     the entity to perform the check for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    static boolean check(@NotNull Entity entity, @NotNull String permission) {
        Objects.requireNonNull(entity, "entity");
        return check(entity.getCommandSource(), permission);
    }

}
