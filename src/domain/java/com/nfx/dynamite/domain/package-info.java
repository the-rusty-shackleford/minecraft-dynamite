/*
 * Dynamite - a three-stick bundle you can throw.
 * Copyright (C) 2026 Rusty Shackleford and nfx
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * The pure layer: what this mod decides, over plain numbers. Compiled against
 * nothing but the JDK. When a fuse goes off ({@link com.nfx.dynamite.domain.Fuse}),
 * how a thrown bundle bounces and comes to rest ({@link com.nfx.dynamite.domain.Bounce}),
 * and what a blast is ({@link com.nfx.dynamite.domain.Blast}).
 */
package com.nfx.dynamite.domain;
