package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.domain.entity.Drone;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyRosterTest {

    private final EnemyRoster roster = new EnemyRoster();

    private static Enemy droneAt(double screenX, double screenY) {
        Drone drone = new Drone();
        drone.setBoundPos(new Vector2D(screenX, screenY));
        return drone;
    }

    @Test
    void checkCollided_returnsHitEnemy() {
        Enemy far = droneAt(500, 500);
        Enemy hit = droneAt(100, 100);
        roster.add(far);
        roster.add(hit);

        assertSame(hit, roster.checkCollided(new Rectangle(95, 95, 10, 10)));
    }

    @Test
    void checkCollided_noHit_returnsNull() {
        roster.add(droneAt(100, 100));

        assertNull(roster.checkCollided(new Rectangle(300, 300, 10, 10)));
    }

    @Test
    void checkCollided_emptyRoster_returnsNull() {
        assertNull(roster.checkCollided(new Rectangle(0, 0, 10, 10)));
    }

    @Test
    void remove_excludesEnemy() {
        Enemy enemy = droneAt(100, 100);
        roster.add(enemy);

        roster.remove(enemy);

        assertTrue(roster.snapshot().isEmpty());
        assertNull(roster.checkCollided(new Rectangle(95, 95, 10, 10)));
    }

    @Test
    void snapshot_keepsInsertionOrder() {
        Enemy first = droneAt(0, 0);
        Enemy second = droneAt(0, 0);
        roster.add(first);
        roster.add(second);

        assertEquals(List.of(first, second), roster.snapshot());
    }

    @Test
    void snapshot_isIndependentCopy() {
        Enemy enemy = droneAt(0, 0);
        roster.add(enemy);

        List<Enemy> snapshot = roster.snapshot();
        snapshot.clear();

        assertEquals(List.of(enemy), roster.snapshot());
    }
}
