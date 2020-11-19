import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.entity.optional.Optional
import com.gitlab.kordlib.rest.builder.guild.EmojiModifyBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EmojiModifyBuilderTest {

    @Test
    fun `builder does not create empty roles by default`() {
        val builder = EmojiModifyBuilder()

        val request = builder.toRequest()

        Assertions.assertEquals(Optional.Missing<Iterable<Snowflake>>(), request.roles)
    }

}