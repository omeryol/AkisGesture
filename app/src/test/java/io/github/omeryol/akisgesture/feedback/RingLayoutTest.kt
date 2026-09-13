package io.github.omeryol.akisgesture.feedback

import io.github.omeryol.akisgesture.overlay.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

/**
 * RingLayout sözleşmesini doğrular: simetri, üst üste binmeme ve sınır mesafesi.
 *
 * Bu testler, kullanıcı bildirimindeki üç sorunu kilitler:
 * 1. Baloncuklar büyütüldüğünde üst üste binme,
 * 2. Kenar boyunca simetrik olmayan aralıklar,
 * 3. Tetik kenarına/sınırlara taşma.
 */
class RingLayoutTest {

    private data class Screen(val width: Float, val height: Float, val density: Float)

    private val screens = listOf(
        Screen(1080f, 2400f, 3f),
        Screen(720f, 1280f, 2f),
        Screen(480f, 800f, 1.5f),
    )

    private val edges = listOf(Edge.LEFT, Edge.RIGHT, Edge.BOTTOM)

    private fun spec(
        edge: Edge,
        screen: Screen,
        count: Int,
        radiusDp: Float = 58f,
        iconScale: Float = 1f,
        spacingDp: Float = 60f,
        insetDp: Float = 100f,
        arc: Float = 0.92f,
        touchRatio: Float = 0.5f,
    ): RingLayout.Spec {
        val alongExtent = if (edge == Edge.BOTTOM) screen.width else screen.height
        return RingLayout.Spec(
            edge = edge,
            alongExtent = alongExtent,
            depthExtent = if (edge == Edge.BOTTOM) screen.height else screen.width,
            touchAlong = alongExtent * touchRatio,
            count = count,
            // Üretimdeki çağrıyla aynı: boyut alanı yarıçap (px) olarak kullanılır.
            radius = radiusDp * iconScale,
            spacing = spacingDp * screen.density,
            baseDepth = insetDp * screen.density,
            arc = arc,
            edgeGap = RingLayout.EDGE_GAP_DP * screen.density,
            minIconGap = RingLayout.MIN_ICON_GAP_DP * screen.density,
        )
    }

    private fun assertInvariants(spec: RingLayout.Spec, layout: RingLayout.Layout) {
        val count = layout.count
        assertTrue("Yerleşim boş olmamalı", count > 0)
        assertEquals("Öğe sayısı korunmalı", spec.count, count)

        // 1) Komşular arası mesafe: seçili baloncuk büyüse bile üst üste binmez.
        //    Gereken mesafe = büyümüş yarıçap + komşu yarıçapı + boşluk tabanı.
        val minSpacing = layout.radius * (2f + RingLayout.SELECTED_GROWTH) + layout.gapFloor
        for (index in 0 until count - 1) {
            val first = layout.items[index]
            val second = layout.items[index + 1]
            val distance = hypot(
                (second.along - first.along).toDouble(),
                (second.depth - first.depth).toDouble(),
            ).toFloat()
            assertTrue(
                "Komşu mesafesi çok küçük ($distance < $minSpacing)",
                distance >= minSpacing - 0.5f,
            )
        }

        // 2) Ölçülen boşluk: asla negatif olamaz ve seçili büyümesi onu tüketemez.
        assertTrue("Boşluk tabanı pozitif olmalı", layout.gapFloor > 0f)
        assertTrue("Ölçülen boşluk negatif olamaz: ${layout.gap}", layout.gap > 0f)
        assertTrue(
            "Temel boşluk en az istenen taban kadar olmalı: ${layout.gap}",
            layout.gap >= layout.gapFloor - 0.01f,
        )
        assertTrue(
            "Seçili büyümesi boşluğu tüketmemeli: ${layout.gapWithSelection}",
            layout.gapWithSelection >= layout.gapFloor - 0.01f,
        )

        // 3) Simetri: grup merkezine göre aynalanan öğeler eş yarıçaplı ve eş uzaklıkta.
        for (index in 0 until count) {
            val first = layout.items[index]
            val mirror = layout.items[count - 1 - index]
            assertEquals("Aynalanan derinlik", first.depth, mirror.depth, 0.05f)
            assertEquals(
                "Aynalanan kenar konumu",
                layout.centerAlong - first.along,
                mirror.along - layout.centerAlong,
                0.05f,
            )
        }
        if (count % 2 == 1) {
            assertEquals(
                "Orta öğe tam merkezde olmalı",
                layout.centerAlong,
                layout.items[count / 2].along,
                0.05f,
            )
        }

        // 4) Sınırlar: tetik kenarına ve ekran kenarlarına taşma yok.
        val boundary = layout.radius * (1f + RingLayout.SELECTED_GROWTH) + spec.edgeGap
        for (item in layout.items) {
            assertTrue("Kenar payı korunmadı", item.depth - layout.radius >= spec.edgeGap - 0.5f)
            assertTrue(
                "Derinlik ekran dışına taşıyor",
                item.depth + layout.radius <= spec.depthExtent - spec.edgeGap + 0.5f,
            )
            assertTrue("Kenar boyu alt sınır", item.along >= boundary - 0.5f)
            assertTrue(
                "Kenar boyu üst sınır",
                item.along <= spec.alongExtent - boundary + 0.5f,
            )
        }
    }

    @Test
    fun `measured gap stays positive even when zero gap is requested`() {
        for (edge in edges) {
            for (count in 2..6) {
                val zeroGap = RingLayout.Spec(
                    edge = edge,
                    alongExtent = 1080f,
                    depthExtent = 2400f,
                    touchAlong = 540f,
                    count = count,
                    radius = 200f,
                    spacing = 0f,
                    baseDepth = 0f,
                    arc = 1f,
                    edgeGap = 0f,
                    minIconGap = 0f,
                )
                val layout = RingLayout.compute(zeroGap)
                assertTrue("Boşluk pozitif kalmalı: ${layout.gap}", layout.gap > 0f)
                assertTrue(
                    "Seçili büyümesiyle bile boşluk negatif olamaz: ${layout.gapWithSelection}",
                    layout.gapWithSelection >= 0f,
                )
                assertInvariants(zeroGap, layout)
            }
        }
    }

    @Test
    fun `effective spacing is measured from the bubble size`() {
        for (edge in edges) {
            var previous = 0f
            for (iconScale in listOf(0.5f, 1f, 1.5f, 2f, 3f)) {
                val spec = spec(
                    edge = edge,
                    screen = screens[0],
                    count = 3,
                    iconScale = iconScale,
                    // Kullanıcı aralığı en küçük değerde tutsa bile ölçü boyuta uyar.
                    spacingDp = 0f,
                )
                val layout = RingLayout.compute(spec)
                assertTrue(
                    "Ölçülen mesafe yarıçapa göre türetilmeli",
                    layout.spacing >= 2f * layout.radius + layout.gapFloor - 0.01f,
                )
                assertTrue(
                    "Boyut büyüdükçe mesafe küçülemez ($previous -> ${layout.spacing})",
                    layout.spacing >= previous - 0.01f,
                )
                previous = layout.spacing
                assertInvariants(spec, layout)
            }
        }
    }

    @Test
    fun `every edge, count, size and arc keeps the layout contract`() {
        for (edge in edges) {
            for (screen in screens) {
                for (count in 2..6) {
                    for (iconScale in listOf(0.5f, 1f, 1.5f, 2f)) {
                        for (arc in listOf(0f, 0.35f, 0.92f, 1f)) {
                            val spec = spec(
                                edge = edge,
                                screen = screen,
                                count = count,
                                iconScale = iconScale,
                                arc = arc,
                            )
                            assertInvariants(spec, RingLayout.compute(spec))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `enlarged bubbles never overlap and stay inside the screen`() {
        for (edge in edges) {
            for (count in 2..6) {
                for (iconScale in listOf(2f, 3f, 4f)) {
                    val spec = spec(
                        edge = edge,
                        screen = Screen(1080f, 2400f, 3f),
                        count = count,
                        iconScale = iconScale,
                    )
                    assertInvariants(spec, RingLayout.compute(spec))
                }
            }
        }
    }

    @Test
    fun `configured spacing is expanded when the bubbles would overlap`() {
        val spec = spec(
            edge = Edge.LEFT,
            screen = Screen(1080f, 2400f, 3f),
            count = 3,
            radiusDp = 200f,
            spacingDp = 20f,
        )
        val layout = RingLayout.compute(spec)
        val minimum = layout.radius * (1f + RingLayout.SELECTED_GROWTH) + spec.minIconGap
        assertTrue(
            "Aralık üst üste binmeyi engelleyecek kadar büyütülmeli",
            layout.spacing >= minimum - 0.5f,
        )
    }

    @Test
    fun `radius is trimmed when the group cannot fit the edge`() {
        val spec = spec(
            edge = Edge.BOTTOM,
            screen = Screen(480f, 800f, 1.5f),
            count = 6,
            radiusDp = 400f,
            iconScale = 2f,
        )
        val layout = RingLayout.compute(spec)
        assertTrue("Yarıçap kırpılmalı", layout.radius < spec.radius)
        assertInvariants(spec, layout)
    }

    @Test
    fun `arc zero produces a straight line`() {
        val spec = spec(edge = Edge.LEFT, screen = screens[0], count = 5, arc = 0f)
        val layout = RingLayout.compute(spec)
        val reference = layout.items.first().depth
        for (item in layout.items) {
            assertEquals("Düz çizgide derinlik sabit olmalı", reference, item.depth, 0.01f)
        }
        assertInvariants(spec, layout)
    }

    @Test
    fun `curvature deepens the middle bubble and fades towards the edges`() {
        val spec = spec(edge = Edge.LEFT, screen = screens[0], count = 5, arc = 1f)
        val layout = RingLayout.compute(spec)
        val depths = layout.items.map { it.depth }
        val middle = depths[depths.size / 2]
        val shaft = depths.first()

        assertTrue("Yay içe doğru şişmeli", middle > shaft + 1f)
        // Orta noktadan kenarlara doğru derinlik tek düze azalmalı: doğal yay.
        for (index in 0 until depths.size / 2) {
            assertTrue(
                "Derinlik kenara doğru azalmalı",
                depths[index] <= depths[index + 1] + 0.01f,
            )
            assertTrue(
                "Derinlik kenara doğru azalmalı (ayna)",
                depths[depths.size - 1 - index] <= depths[depths.size - 2 - index] + 0.01f,
            )
        }
        assertInvariants(spec, layout)
    }

    @Test
    fun `configured inset is honoured when there is room`() {
        val screen = screens[0]
        for (arc in listOf(0f, 0.92f, 1f)) {
            val spec = spec(edge = Edge.RIGHT, screen = screen, count = 3, arc = arc)
            val layout = RingLayout.compute(spec)
            val closest = layout.items.minOf { it.depth }
            assertEquals(
                "Grubun kenara en yakın noktası ayarlanan iç boşluk olmalı",
                spec.baseDepth,
                closest,
                0.5f,
            )
        }
    }

    @Test
    fun `group slides as a whole near the edges`() {
        val screen = screens[0]
        for (edge in edges) {
            val atStart = RingLayout.compute(spec(edge = edge, screen = screen, count = 3, touchRatio = 0f))
            val atEnd = RingLayout.compute(spec(edge = edge, screen = screen, count = 3, touchRatio = 1f))

            val extent = if (edge == Edge.BOTTOM) screen.width else screen.height
            val lowerInset = atStart.items.minOf { it.along }
            val upperInset = extent - atEnd.items.maxOf { it.along }
            assertEquals(
                "İki uçtaki sınır mesafesi eşit olmalı",
                lowerInset,
                upperInset,
                0.5f,
            )

            // Grup bir bütün olarak kaydırılır: iç aralıklar korunur.
            val spanStart = atStart.items.last().along - atStart.items.first().along
            val spanEnd = atEnd.items.last().along - atEnd.items.first().along
            assertEquals("Kaydırma grubu sıkıştırmamalı", spanStart, spanEnd, 0.5f)
        }
    }

    @Test
    fun `centred touch centres the group symmetrically`() {
        for (edge in edges) {
            for (count in 2..6) {
                val spec = spec(edge = edge, screen = screens[0], count = count, touchRatio = 0.5f)
                val layout = RingLayout.compute(spec)
                assertEquals(
                    "Grup, parmağın kenar konumuna ortalanmalı",
                    spec.alongExtent / 2f,
                    layout.centerAlong,
                    0.5f,
                )
                assertInvariants(spec, layout)
            }
        }
    }

    @Test
    fun `screen mapping mirrors opposite edges`() {
        val width = 1080f
        val height = 2400f
        val left = RingLayout.compute(spec(edge = Edge.LEFT, screen = screens[0], count = 3))
        val right = RingLayout.compute(spec(edge = Edge.RIGHT, screen = screens[0], count = 3))
        val bottom = RingLayout.compute(spec(edge = Edge.BOTTOM, screen = screens[0], count = 3))

        for (index in 0 until left.count) {
            val (leftX, leftY) = left.center(index, Edge.LEFT, width, height)
            val (rightX, rightY) = right.center(index, Edge.RIGHT, width, height)
            assertEquals("Sol/sağ aynalanmalı", width - rightX, leftX, 0.5f)
            assertEquals("Sol/sağ kenar ekseni aynı", leftY, rightY, 0.5f)

            val (bottomX, bottomY) = bottom.center(index, Edge.BOTTOM, width, height)
            assertEquals("Alt kenar yatay eksen", bottom.items[index].along, bottomX, 0.5f)
            assertEquals(
                "Alt kenar derinliği yukarı doğru ölçülür",
                height - bottom.items[index].depth,
                bottomY,
                0.5f,
            )
        }
    }
}
