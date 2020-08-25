import com.gitlab.kordlib.common.annotation.KordUnstableApi
import com.gitlab.kordlib.rest.builder.guild.EmojiModifyBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@OptIn(KordUnstableApi::class)
class EmojiModifyBuilderTest {

    @Test
    fun `builder does not create empty roles by default`() {
        val builder = EmojiModifyBuilder()

        val request = builder.toRequest()

        Assertions.assertEquals(null, request.roles)
    }

}