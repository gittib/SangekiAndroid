package work.boardgame.sangeki_rooper.activity

import junit.framework.TestCase
import org.junit.Test
import work.boardgame.sangeki_rooper.fragment.ScenarioDetailFragment

class ContainerActivityTest : TestCase() {

    @Test
    fun testGetFragment() {
        val act = ContainerActivity()
        val method = act.javaClass.getDeclaredMethod("getFragment", String::class.java, Any::class.java)
        method.isAccessible = true

        method.invoke(act, ScenarioDetailFragment::class.qualifiedName, "id")
    }
}