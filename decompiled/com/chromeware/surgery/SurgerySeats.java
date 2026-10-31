/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.catnip.levelWrappers.SchematicLevelAccessor
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 */
package com.chromeware.surgery;

import com.chromeware.surgery.SurgerySeat;
import java.util.List;
import net.createmod.catnip.levelWrappers.SchematicLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class SurgerySeats {
    public static List<SurgerySeat> in(Level level, AABB area) {
        if (level instanceof SchematicLevelAccessor) {
            SchematicLevelAccessor schematic = (SchematicLevelAccessor)level;
            return schematic.getEntityList().stream().filter(SurgerySeat.class::isInstance).map(SurgerySeat.class::cast).filter(s -> s.isAlive() && area.intersects(s.getBoundingBox())).toList();
        }
        return level.getEntitiesOfClass(SurgerySeat.class, area);
    }
}

