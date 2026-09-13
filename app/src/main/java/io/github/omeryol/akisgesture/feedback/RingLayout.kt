package io.github.omeryol.akisgesture.feedback

import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Halka menüsü ve son uygulamalar şeridinin yerleşim geometrisi.
 *
 * Bu nesne **tek yetkili kaynaktır**: canlı çizim (`RingMenuRenderer`), dokunma
 * testi (`GestureEngine.ringHitTest`) ve ayarlar önizlemesi
 * (`InteractivePhoneMap.drawRingPreviews`) aynı hesabı kullanır. Böylece
 * görünen yerleşim ile dokunulan yerleşim birbirinden ayrışamaz.
 *
 * Sözleşme (birim testleriyle doğrulanır):
 * 1. Merkez mesafesi boyuta göre **dinamik ölçülür**: komşu baloncuklar arasındaki
 *    mesafe her zaman `radius * (1 + SELECTED_GROWTH) + boşluk` alt sınırını korur.
 *    Seçili baloncuk nabızla büyürken bile üst üste binme olmaz.
 * 2. Ölçülen boşluk ([Layout.gap]) hiçbir ayarda negatif olamaz; çağıran sıfır
 *    boşluk istese bile [ABSOLUTE_GAP_RATIO] tabanı geçerlidir ve en az 1 dp'lik
 *    boşluk pratikte her zaman sağlanır.
 * 3. Yerleşim, grup merkezine göre kenar boyunca simetriktir; aynı kenar
 *    yansımasındaki baloncuklar kenardan eşit uzaklıktadır.
 * 4. Hiçbir baloncuk tetik kenarına `edgeGap` + yarıçap mesafesinden daha fazla
 *    yaklaşmaz ve ekran sınırlarının dışına taşmaz.
 * 5. Grup, parmağın kenar üzerindeki konumuna göre ortalanır; sınıra
 *    yaklaşıldığında grup bir bütün olarak kaydırılır (tek tek kırpılmaz).
 *
 * Koordinat sistemi: `along` = tetik kenarı boyunca (kenarın uzun ekseni),
 * `depth` = tetik kenarından ekran içine doğru dik uzaklık. Bu iki değer
 * [Layout.center] ile ekran koordinatına çevrilir.
 *
 * Birim: tüm değerler pikseldir. Çağıran taraf dp değerlerini yoğunlukla
 * çarparak verir; `ringSizeDp` alanı mevcut davranışı korumak için ölçek dahil
 * yarıçap olarak (px) alınır.
 */
object RingLayout {

    /** `arc = 1` iken yayın taradığı toplam açı. `arc = 0` düz çizgi demektir. */
    const val MAX_ARC_DEGREES = 80f

    /** Seçili baloncuğun büyüme katsayısı (nabız hariç). */
    const val SELECTED_SCALE = 1.24f

    /** Nabız sırasında eklenen en büyük ek büyüme. */
    const val SELECTED_PULSE = 0.05f

    /** Yerleşimin hesaba kattığı en büyük seçili büyümesi. */
    const val SELECTED_GROWTH = SELECTED_SCALE + SELECTED_PULSE - 1f

    /** Tetik kenarı ile en yakın baloncuk yüzeyi arasında korunan boşluk (dp). */
    const val EDGE_GAP_DP = 6f

    /** Baloncuklar arasında istenen en küçük boşluk (dp). */
    const val MIN_ICON_GAP_DP = 8f

    /**
     * Yarıçapa oranlı **mutlak** boşluk tabanı. Çağıran sıfır boşluk istese bile
     * komşu baloncuk yüzeyleri arasında her zaman pozitif bir boşluk kalır;
     * ölçülen boşluk hiçbir koşulda negatif olamaz.
     */
    const val ABSOLUTE_GAP_RATIO = 0.02f

    private const val DEGREES_TO_RADIANS = (Math.PI / 180.0).toFloat()
    private const val STRAIGHT_EPSILON = 1e-4f

    data class Spec(
        val edge: Edge,
        /** Tetik kenarının uzunluğu (px). */
        val alongExtent: Float,
        /** Tetik kenarından içeri kullanılabilir derinlik (px). */
        val depthExtent: Float,
        /** Parmağın kenar üzerindeki konumu (px). */
        val touchAlong: Float,
        val count: Int,
        /** Baloncuk yarıçapı (px, ölçek dahil). */
        val radius: Float,
        /** İstenen merkez mesafesi (px). Alt sınır her zaman uygulanır. */
        val spacing: Float,
        /** Grubun kenara en yakın baloncuk merkezinin derinliği (px). */
        val baseDepth: Float,
        /** 0 = düz çizgi, 1 = en belirgin yay. */
        val arc: Float,
        val edgeGap: Float = 0f,
        val minIconGap: Float = 0f,
    )

    data class Item(
        val along: Float,
        val depth: Float,
        val angleRadians: Float,
    )

    data class Layout(
        /** Ekrana sığdırmak için kırpılmış olabilecek gerçek yarıçap. */
        val radius: Float,
        /** Kırpılmış olabilecek gerçek merkez mesafesi (boyuta göre dinamik ölçülür). */
        val spacing: Float,
        val baseDepth: Float,
        val halfExtent: Float,
        val centerAlong: Float,
        /** Uygulanan boşluk tabanı (çağıranın isteği ile mutlak taban arasından büyük olan). */
        val gapFloor: Float,
        val items: List<Item>,
    ) {
        val count: Int get() = items.size

        /** Komşu baloncuk yüzeyleri arasındaki ölçülen boşluk. Hiçbir zaman negatif olamaz. */
        val gap: Float get() = spacing - 2f * radius

        /** Seçili baloncuk en büyük hâlindeyken kalan en küçük boşluk. */
        val gapWithSelection: Float get() = spacing - radius * (2f + SELECTED_GROWTH)

        /** Öğe merkezini ekran koordinatına çevirir. */
        fun center(index: Int, edge: Edge, width: Float, height: Float): Pair<Float, Float> {
            val item = items[index.coerceIn(0, items.lastIndex)]
            return when (edge) {
                Edge.LEFT -> item.depth to item.along
                Edge.RIGHT -> width - item.depth to item.along
                Edge.BOTTOM -> item.along to height - item.depth
            }
        }
    }

    fun compute(spec: Spec): Layout {
        val count = spec.count.coerceAtLeast(1)
        val edgeGap = spec.edgeGap.coerceAtLeast(0f)
        val iconGap = spec.minIconGap.coerceAtLeast(0f)
        val alongExtent = spec.alongExtent.coerceAtLeast(1f)
        val depthExtent = spec.depthExtent.coerceAtLeast(1f)

        // 1) Yarıçap: grup, iki yanda kenar payı ve arada en küçük boşluklarla
        //    birlikte sığmalı. Sığmıyorsa yarıçap kırpılır; aralık değil, çünkü
        //    üst üste binmeyi engelleyen ölçü aralıktır.
        val growth = SELECTED_GROWTH
        // Toplam kenar ihtiyacı: her komşu çifti için (2 + büyüme) yarıçap,
        // iki uçta ise seçili büyümesini de karşılayan (1 + büyüme) yarıçap.
        val fitDenominator = (count - 1) * (2f + growth) + 2f * (1f + growth)
        val radiusFit = (alongExtent - 2f * edgeGap - (count - 1) * iconGap) / fitDenominator
        val radius = min(spec.radius, radiusFit).coerceAtLeast(1f)

        // 2) Boşluk tabanı, çağıranın isteği ne olursa olsun pozitif kalır; ölçülen
        //    boşluk bu nedenle hiçbir koşulda negatif olamaz.
        val gapFloor = max(iconGap, radius * ABSOLUTE_GAP_RATIO)

        // 3) Merkez mesafesi boyuta göre ölçülür: komşu iki baloncuk, seçili olan
        //    nabızla büyüdüğünde bile birbirine değmemelidir.
        //    Gereken mesafe = büyümüş yarıçap + komşu yarıçapı + boşluk.
        val minSpacing = radius * (2f + growth) + gapFloor
        val boundaryRadius = radius * (1f + growth)
        val centerRoom = (alongExtent - 2f * (boundaryRadius + edgeGap)).coerceAtLeast(0f)

        // 3) Eşit açı adımlı yay. arc = 0 -> düz çizgi; arc = 1 -> MAX_ARC_DEGREES.
        val arc = spec.arc.coerceIn(0f, 1f)
        val totalAngle = MAX_ARC_DEGREES * arc * DEGREES_TO_RADIANS
        val stepAngle = if (count > 1) totalAngle / (count - 1) else 0f
        val halfAngle = if (count > 1) totalAngle / 2f else 0f
        val curved = count > 1 && stepAngle > STRAIGHT_EPSILON

        fun arcRadius(spacing: Float): Float =
            if (curved) spacing / (2f * sin(stepAngle / 2f)) else Float.POSITIVE_INFINITY

        fun halfExtent(spacing: Float): Float =
            if (curved) arcRadius(spacing) * sin(halfAngle) else spacing * (count - 1) / 2f

        // 4) İstenen aralık, üst üste binmeyi engelleyen alt sınırla birlikte
        //    kullanılır. Yay, kenar boyu genişliği sığmıyorsa aralık simetrik
        //    olarak sıkıştırılır (grup merkezi kaymaz).
        var spacing = max(spec.spacing, minSpacing)
        val needed = 2f * halfExtent(spacing)
        if (needed > centerRoom && needed > 0f) {
            spacing *= centerRoom / needed
        }

        // 5) Emniyet: sıkıştırma üst üste binme sınırının altına indiyse yarıçapı
        //    küçült (aşırı dar ekranlarda tek tutarlı çözüm budur).
        val finalRadius = if (spacing < minSpacing) {
            ((spacing - gapFloor) / (2f + growth)).coerceAtLeast(1f)
        } else {
            radius
        }
        val finalSpacing = max(spacing, finalRadius * (2f + growth) + gapFloor)

        val rho = arcRadius(finalSpacing)
        val extent = halfExtent(finalSpacing)
        val sagitta = if (curved) rho * (1f - cos(halfAngle)) else 0f

        // 6) Derinlik: en yakın baloncuk merkezi kenardan en az yarıçap + pay uzakta.
        val depthMin = finalRadius + edgeGap
        val ceiling = if (spec.edge == Edge.BOTTOM) depthExtent * 0.5f else depthExtent * 0.9f
        val depthMax = max(
            depthMin,
            min(ceiling, depthExtent - finalRadius - edgeGap - sagitta),
        )
        val baseDepth = spec.baseDepth.coerceIn(depthMin, depthMax)

        // 7) Grup merkezi: kenar boyunca bir bütün olarak ve simetrik sınırlanır.
        val limit = finalRadius * (1f + growth) + edgeGap + extent
        val centerMin = limit
        val centerMax = max(centerMin, alongExtent - limit)
        val centerAlong = spec.touchAlong.coerceIn(centerMin, centerMax)

        // 8) Yerleşim.
        val middle = (count - 1) / 2f
        val items = (0 until count).map { index ->
            val offset = index - middle
            if (!curved) {
                Item(
                    along = centerAlong + offset * finalSpacing,
                    depth = baseDepth,
                    angleRadians = 0f,
                )
            } else {
                val phi = offset * stepAngle
                Item(
                    along = centerAlong + rho * sin(phi),
                    depth = baseDepth + rho * (cos(phi) - cos(halfAngle)),
                    angleRadians = phi,
                )
            }
        }

        return Layout(
            radius = finalRadius,
            spacing = finalSpacing,
            baseDepth = baseDepth,
            halfExtent = extent,
            centerAlong = centerAlong,
            gapFloor = gapFloor,
            items = items,
        )
    }
}
