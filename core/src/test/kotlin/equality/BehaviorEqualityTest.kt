package equality

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Entity
import kotlin.test.Test
import kotlin.test.assertEquals

interface BehaviorEqualityTest<T: Entity> : EntityEqualityTest<T> {

    fun T.behavior() : Entity

    @Test
    fun `Full entity equals its behavior`(){
        val entity = newEntity(randomId())
        val behavior = entity.behavior()

        assertEquals(entity, behavior)
    }

    @Test
    fun `Behavior equals its full entity`(){
        val entity = newEntity(randomId())
        val behavior = entity.behavior()

        assertEquals(behavior, entity)
    }

}