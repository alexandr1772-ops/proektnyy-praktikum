package ru.attendance.tracker.ui.login

// Векторная графика экрана входа. Формы сняты с макетов в mobile/mockups и описаны командами пути:
// 0 — moveTo(x, y), 1 — lineTo(x, y), 2 — quadTo(x1, y1, x, y), 3 — cubicTo(x1, y1, x2, y2, x, y), 4 — close.
// Иллюстрация — в пикселях картинки 548 x 250, логотип — 230 x 226, волны — в пикселях макета шириной 863.

/** Цвета иллюстрации: индекс в палитре. */
internal object ArtColor {
    const val BLOB = 0
    const val HAZE = 1
    const val GROUND = 2
    const val LEAF_A = 3
    const val LEAF_B = 4
    const val VEIN = 5
    const val FLAP_TOP = 6
    const val FLAP_BOTTOM = 7
    const val STACK = 8
    const val PAGE = 9
    const val PAGE_SHADE = 10
    const val HEADER_TOP = 11
    const val HEADER_BOTTOM = 12
    const val CELL_A = 13
    const val CELL_B = 14
    const val RING = 15
    const val RING_HI = 16
    const val RIVET = 17
    const val COVER_TOP = 18
    const val COVER = 19
    const val COVER_DARK = 20
    const val SPINE_HI = 21
    const val PALE_COVER = 22
    const val PALE_COVER_DARK = 23
    const val PALE_SPINE_HI = 24
    const val PAGES = 25
    const val PAGES_SHADE = 26
    const val PAGE_LINE = 27
    const val MARK_DARK = 28
    const val MARK_TEAL = 29
    const val CHECK = 30
    const val PAPER_BACK = 31
}

internal val IllustrationLight = intArrayOf(
    0xFFEAF2FC.toInt(), // blob
    0xFFE4EEFA.toInt(), // haze
    0xFFE2EDFA.toInt(), // ground
    0xFFC7DDF6.toInt(), // leafA
    0xFFB4D2F2.toInt(), // leafB
    0xFF9DC3EC.toInt(), // vein
    0xFF5B9BE2.toInt(), // flapTop
    0xFF3C84D3.toInt(), // flapBottom
    0xFFD3E4F7.toInt(), // stack
    0xFFFFFFFF.toInt(), // page
    0xFFF3F7FC.toInt(), // pageShade
    0xFF6AA9EA.toInt(), // headerTop
    0xFF5A9DE4.toInt(), // headerBottom
    0xFFDCEAFB.toInt(), // cellA
    0xFFE9F2FD.toInt(), // cellB
    0xFF2F74C4.toInt(), // ring
    0xFF4C8FD8.toInt(), // ringHi
    0xFF2A69B4.toInt(), // rivet
    0xFF5297DF.toInt(), // coverTop
    0xFF3A82D2.toInt(), // cover
    0xFF2F72C0.toInt(), // coverDark
    0xFF5C9EE2.toInt(), // spineHi
    0xFFC4DAF3.toInt(), // paleCover
    0xFFAECCEE.toInt(), // paleCoverDark
    0xFFD6E6F8.toInt(), // paleSpineHi
    0xFFFFFFFF.toInt(), // pages
    0xFFE9F0F9.toInt(), // pagesShade
    0xFFD3E0EF.toInt(), // pageLine
    0xFF3387CE.toInt(), // markDark
    0xFF93CEDF.toInt(), // markTeal
    0xFFFFFFFF.toInt(), // check
    0xFFF0F4F9.toInt(), // paperBack
)

internal val IllustrationDark = intArrayOf(
    0xFF15233A.toInt(), // blob
    0xFF13203A.toInt(), // haze
    0xFF142238.toInt(), // ground
    0xFF1D3658.toInt(), // leafA
    0xFF22416A.toInt(), // leafB
    0xFF2D5583.toInt(), // vein
    0xFF3F7FC9.toInt(), // flapTop
    0xFF2C66AD.toInt(), // flapBottom
    0xFF93A9C4.toInt(), // stack
    0xFFDCE5F0.toInt(), // page
    0xFFC8D5E5.toInt(), // pageShade
    0xFF4F8FDB.toInt(), // headerTop
    0xFF4282D0.toInt(), // headerBottom
    0xFFB9CDE6.toInt(), // cellA
    0xFFC9D9EC.toInt(), // cellB
    0xFF7DB0EC.toInt(), // ring
    0xFF9CC4F2.toInt(), // ringHi
    0xFF5E97DD.toInt(), // rivet
    0xFF4087D6.toInt(), // coverTop
    0xFF2E70BE.toInt(), // cover
    0xFF255FA5.toInt(), // coverDark
    0xFF4A8FDA.toInt(), // spineHi
    0xFF3D5A80.toInt(), // paleCover
    0xFF324D70.toInt(), // paleCoverDark
    0xFF4A6A93.toInt(), // paleSpineHi
    0xFFDCE5F0.toInt(), // pages
    0xFFC2D0E2.toInt(), // pagesShade
    0xFF9FB2CA.toInt(), // pageLine
    0xFF2F7FD0.toInt(), // markDark
    0xFF5DB3C9.toInt(), // markTeal
    0xFFFFFFFF.toInt(), // check
    0xFFB9C7D9.toInt(), // paperBack
)

internal enum class ArtLayer { Back, Calendar, Page, Rings, Books }

/** Фигура: путь, заливка (цвет или линейный градиент между двумя цветами палитры) и/или обводка. */
internal class ArtShape(
    val layer: ArtLayer,
    val path: FloatArray,
    val fill: ArtFill? = null,
    val stroke: Int = -1,
    val strokeWidth: Float = 0f,
)

internal class ArtFill(
    val from: Int,
    val to: Int = from,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
)

internal val IllustrationShapes = listOf(
    // blob
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 296f, 70f,
            3f, 300.33f, 54.67f, 307.33f, 38.33f, 318f, 28f,
            3f, 328.67f, 17.67f, 345f, 12.33f, 360f, 8f,
            3f, 375f, 3.67f, 393.33f, 1.33f, 408f, 2f,
            3f, 422.67f, 2.67f, 437.67f, 4.67f, 448f, 12f,
            3f, 458.33f, 19.33f, 466.67f, 32f, 470f, 46f,
            3f, 473.33f, 60f, 470f, 81.33f, 468f, 96f,
            3f, 466f, 110.67f, 457f, 120.67f, 458f, 134f,
            3f, 459f, 147.33f, 472.33f, 162.67f, 474f, 176f,
            3f, 475.67f, 189.33f, 475.33f, 205.67f, 468f, 214f,
            3f, 460.67f, 222.33f, 453f, 224f, 430f, 226f,
            3f, 407f, 228f, 351.67f, 233.67f, 330f, 226f,
            3f, 308.33f, 218.33f, 306.33f, 197.67f, 300f, 180f,
            3f, 293.67f, 162.33f, 292.67f, 138.33f, 292f, 120f,
            3f, 291.33f, 101.67f, 291.67f, 85.33f, 296f, 70f,
            4f,
        ),
        fill = ArtFill(ArtColor.BLOB, ArtColor.HAZE, 380f, 0f, 380f, 226f),
    ),
    // haze
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, -10f, 176f,
            3f, -4.67f, 162.67f, 10f, 162.67f, 22f, 160f,
            3f, 34f, 157.33f, 49.33f, 157.33f, 62f, 160f,
            3f, 74.67f, 162.67f, 88.67f, 167f, 98f, 176f,
            3f, 107.33f, 185f, 116.67f, 203.33f, 118f, 214f,
            3f, 119.33f, 224.67f, 119f, 234.67f, 106f, 240f,
            3f, 93f, 245.33f, 59.33f, 246f, 40f, 246f,
            3f, 20.67f, 246f, -1.67f, 251.67f, -10f, 240f,
            3f, -18.33f, 228.33f, -15.33f, 189.33f, -10f, 176f,
            4f,
        ),
        fill = ArtFill(ArtColor.HAZE),
    ),
    // ground
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 70f, 236f,
            3f, 90f, 233.5f, 151.67f, 230.33f, 200f, 229f,
            3f, 248.33f, 227.67f, 310f, 227.5f, 360f, 228f,
            3f, 410f, 228.5f, 473.33f, 230f, 500f, 232f,
            3f, 526.67f, 234f, 540f, 237.5f, 520f, 240f,
            3f, 500f, 242.5f, 433.33f, 245.83f, 380f, 247f,
            3f, 326.67f, 248.17f, 250f, 247.5f, 200f, 247f,
            3f, 150f, 246.5f, 101.67f, 245.83f, 80f, 244f,
            3f, 58.33f, 242.17f, 50f, 238.5f, 70f, 236f,
            4f,
        ),
        fill = ArtFill(ArtColor.GROUND),
    ),
    // leafL1
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 94f, 198f,
            1f, 93.88f, 189.37f,
            1f, 94.16f, 180.6f,
            1f, 94.54f, 171.78f,
            1f, 94.89f, 162.98f,
            1f, 95.13f, 154.22f,
            1f, 95.21f, 145.52f,
            1f, 95.05f, 136.91f,
            1f, 94.61f, 128.4f,
            1f, 93.84f, 120.01f,
            1f, 92.71f, 111.76f,
            1f, 91.18f, 103.66f,
            1f, 89.23f, 95.72f,
            1f, 86.83f, 87.93f,
            1f, 83.99f, 80.32f,
            1f, 81.25f, 74.04f,
            1f, 78.25f, 67.94f,
            1f, 75.01f, 62.06f,
            1f, 71.56f, 56.47f,
            1f, 67.96f, 51.21f,
            1f, 64.23f, 46.33f,
            1f, 60.42f, 41.89f,
            1f, 56.58f, 37.9f,
            1f, 52.75f, 34.42f,
            1f, 48.98f, 31.48f,
            1f, 45.32f, 29.08f,
            1f, 41.81f, 27.27f,
            1f, 38.48f, 26.05f,
            1f, 35.38f, 25.43f,
            1f, 32.55f, 25.41f,
            1f, 30f, 26f,
            1f, 27.77f, 27.19f,
            1f, 25.88f, 28.96f,
            1f, 24.34f, 31.31f,
            1f, 23.16f, 34.21f,
            1f, 22.35f, 37.63f,
            1f, 21.91f, 41.55f,
            1f, 21.83f, 45.93f,
            1f, 22.12f, 50.73f,
            1f, 22.75f, 55.9f,
            1f, 23.7f, 61.41f,
            1f, 24.98f, 67.2f,
            1f, 26.54f, 73.22f,
            1f, 28.37f, 79.42f,
            1f, 30.45f, 85.72f,
            1f, 32.75f, 92.09f,
            1f, 35.25f, 98.46f,
            1f, 38.46f, 105.93f,
            1f, 41.95f, 113.31f,
            1f, 45.71f, 120.58f,
            1f, 49.71f, 127.76f,
            1f, 53.93f, 134.87f,
            1f, 58.34f, 141.9f,
            1f, 62.89f, 148.87f,
            1f, 67.56f, 155.81f,
            1f, 72.28f, 162.72f,
            1f, 76.99f, 169.64f,
            1f, 81.64f, 176.58f,
            1f, 86.13f, 183.58f,
            1f, 90.36f, 190.68f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 30f, 26f, 94f, 198f),
    ),
    // veinL1
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 88.7f, 176.48f,
            2f, 71.34f, 110.48f, 41.67f, 49.07f,
        ),
        stroke = ArtColor.VEIN,
        strokeWidth = 1.3f,
    ),
    // leafL3
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 112f, 200f,
            1f, 112f, 194.04f,
            1f, 112.3f, 188.07f,
            1f, 112.73f, 182.09f,
            1f, 113.23f, 176.1f,
            1f, 113.78f, 170.12f,
            1f, 114.33f, 164.13f,
            1f, 114.86f, 158.14f,
            1f, 115.34f, 152.16f,
            1f, 115.75f, 146.18f,
            1f, 116.07f, 140.2f,
            1f, 116.29f, 134.23f,
            1f, 116.38f, 128.27f,
            1f, 116.36f, 122.32f,
            1f, 116.2f, 116.37f,
            1f, 115.96f, 111.38f,
            1f, 115.65f, 106.44f,
            1f, 115.26f, 101.6f,
            1f, 114.79f, 96.9f,
            1f, 114.25f, 92.4f,
            1f, 113.63f, 88.14f,
            1f, 112.94f, 84.15f,
            1f, 112.18f, 80.48f,
            1f, 111.35f, 77.16f,
            1f, 110.45f, 74.23f,
            1f, 109.49f, 71.71f,
            1f, 108.48f, 69.62f,
            1f, 107.42f, 68f,
            1f, 106.31f, 66.84f,
            1f, 105.17f, 66.18f,
            1f, 104f, 66f,
            1f, 102.82f, 66.32f,
            1f, 101.63f, 67.12f,
            1f, 100.46f, 68.41f,
            1f, 99.31f, 70.17f,
            1f, 98.2f, 72.38f,
            1f, 97.14f, 75.02f,
            1f, 96.15f, 78.07f,
            1f, 95.24f, 81.49f,
            1f, 94.42f, 85.26f,
            1f, 93.71f, 89.33f,
            1f, 93.12f, 93.66f,
            1f, 92.66f, 98.22f,
            1f, 92.34f, 102.96f,
            1f, 92.16f, 107.84f,
            1f, 92.12f, 112.8f,
            1f, 92.24f, 117.8f,
            1f, 92.58f, 123.74f,
            1f, 93.15f, 129.66f,
            1f, 93.93f, 135.57f,
            1f, 94.93f, 141.46f,
            1f, 96.13f, 147.35f,
            1f, 97.51f, 153.22f,
            1f, 99.05f, 159.08f,
            1f, 100.74f, 164.94f,
            1f, 102.55f, 170.79f,
            1f, 104.44f, 176.63f,
            1f, 106.39f, 182.47f,
            1f, 108.35f, 188.31f,
            1f, 110.27f, 194.15f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 104f, 66f, 112f, 200f),
    ),
    // leafL2
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 96f, 204f,
            1f, 92.23f, 199.71f,
            1f, 88.89f, 195.01f,
            1f, 85.76f, 190.1f,
            1f, 82.76f, 185.06f,
            1f, 79.84f, 179.94f,
            1f, 76.95f, 174.8f,
            1f, 74.05f, 169.66f,
            1f, 71.12f, 164.55f,
            1f, 68.11f, 159.52f,
            1f, 65.01f, 154.59f,
            1f, 61.78f, 149.77f,
            1f, 58.42f, 145.08f,
            1f, 54.92f, 140.54f,
            1f, 51.25f, 136.15f,
            1f, 48.06f, 132.57f,
            1f, 44.8f, 129.12f,
            1f, 41.5f, 125.83f,
            1f, 38.19f, 122.74f,
            1f, 34.91f, 119.89f,
            1f, 31.66f, 117.31f,
            1f, 28.48f, 115.03f,
            1f, 25.38f, 113.08f,
            1f, 22.39f, 111.5f,
            1f, 19.53f, 110.3f,
            1f, 16.81f, 109.51f,
            1f, 14.25f, 109.13f,
            1f, 11.88f, 109.2f,
            1f, 9.7f, 109.7f,
            1f, 7.73f, 110.63f,
            1f, 6f, 112f,
            1f, 4.51f, 113.79f,
            1f, 3.28f, 115.97f,
            1f, 2.33f, 118.53f,
            1f, 1.67f, 121.44f,
            1f, 1.31f, 124.67f,
            1f, 1.26f, 128.17f,
            1f, 1.53f, 131.9f,
            1f, 2.13f, 135.83f,
            1f, 3.06f, 139.89f,
            1f, 4.32f, 144.05f,
            1f, 5.91f, 148.26f,
            1f, 7.82f, 152.46f,
            1f, 10.03f, 156.61f,
            1f, 12.55f, 160.67f,
            1f, 15.33f, 164.58f,
            1f, 18.37f, 168.32f,
            1f, 22.28f, 172.46f,
            1f, 26.53f, 176.28f,
            1f, 31.1f, 179.78f,
            1f, 35.99f, 182.97f,
            1f, 41.18f, 185.87f,
            1f, 46.64f, 188.5f,
            1f, 52.36f, 190.88f,
            1f, 58.3f, 193.04f,
            1f, 64.42f, 195.03f,
            1f, 70.69f, 196.87f,
            1f, 77.06f, 198.61f,
            1f, 83.47f, 200.31f,
            1f, 89.86f, 202.04f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 6f, 112f, 96f, 204f),
    ),
    // veinL2
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 82.78f, 195.32f,
            2f, 43.05f, 167.58f, 15.85f, 127.57f,
        ),
        stroke = ArtColor.VEIN,
        strokeWidth = 1.3f,
    ),
    // leafR1
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 338f, 178f,
            1f, 338.46f, 173.43f,
            1f, 339.15f, 168.82f,
            1f, 339.9f, 164.19f,
            1f, 340.65f, 159.57f,
            1f, 341.37f, 154.95f,
            1f, 342.01f, 150.35f,
            1f, 342.54f, 145.76f,
            1f, 342.94f, 141.2f,
            1f, 343.18f, 136.67f,
            1f, 343.25f, 132.17f,
            1f, 343.13f, 127.71f,
            1f, 342.8f, 123.28f,
            1f, 342.25f, 118.9f,
            1f, 341.48f, 114.55f,
            1f, 340.67f, 110.93f,
            1f, 339.72f, 107.36f,
            1f, 338.64f, 103.9f,
            1f, 337.45f, 100.56f,
            1f, 336.16f, 97.38f,
            1f, 334.78f, 94.4f,
            1f, 333.33f, 91.63f,
            1f, 331.82f, 89.1f,
            1f, 330.28f, 86.85f,
            1f, 328.73f, 84.88f,
            1f, 327.17f, 83.23f,
            1f, 325.63f, 81.9f,
            1f, 324.13f, 80.9f,
            1f, 322.68f, 80.25f,
            1f, 321.3f, 79.95f,
            1f, 320f, 80f,
            1f, 318.79f, 80.41f,
            1f, 317.69f, 81.16f,
            1f, 316.71f, 82.26f,
            1f, 315.85f, 83.69f,
            1f, 315.12f, 85.44f,
            1f, 314.52f, 87.49f,
            1f, 314.06f, 89.83f,
            1f, 313.74f, 92.43f,
            1f, 313.56f, 95.26f,
            1f, 313.51f, 98.3f,
            1f, 313.6f, 101.52f,
            1f, 313.82f, 104.9f,
            1f, 314.17f, 108.39f,
            1f, 314.64f, 111.97f,
            1f, 315.22f, 115.6f,
            1f, 315.91f, 119.25f,
            1f, 316.87f, 123.56f,
            1f, 317.99f, 127.84f,
            1f, 319.27f, 132.09f,
            1f, 320.69f, 136.32f,
            1f, 322.24f, 140.52f,
            1f, 323.91f, 144.7f,
            1f, 325.67f, 148.86f,
            1f, 327.5f, 153.01f,
            1f, 329.37f, 157.15f,
            1f, 331.26f, 161.29f,
            1f, 333.13f, 165.44f,
            1f, 334.93f, 169.59f,
            1f, 336.61f, 173.77f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 320f, 80f, 338f, 178f),
    ),
    // leafR3
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 378f, 176f,
            1f, 381.11f, 173.46f,
            1f, 384.43f, 171.02f,
            1f, 387.8f, 168.6f,
            1f, 391.15f, 166.16f,
            1f, 394.43f, 163.7f,
            1f, 397.6f, 161.19f,
            1f, 400.64f, 158.62f,
            1f, 403.52f, 155.97f,
            1f, 406.2f, 153.24f,
            1f, 408.67f, 150.4f,
            1f, 410.91f, 147.46f,
            1f, 412.91f, 144.41f,
            1f, 414.65f, 141.24f,
            1f, 416.14f, 137.96f,
            1f, 417.19f, 135.1f,
            1f, 418.06f, 132.19f,
            1f, 418.74f, 129.27f,
            1f, 419.23f, 126.34f,
            1f, 419.52f, 123.46f,
            1f, 419.63f, 120.64f,
            1f, 419.55f, 117.92f,
            1f, 419.29f, 115.33f,
            1f, 418.86f, 112.88f,
            1f, 418.27f, 110.62f,
            1f, 417.52f, 108.56f,
            1f, 416.64f, 106.72f,
            1f, 415.63f, 105.14f,
            1f, 414.52f, 103.81f,
            1f, 413.3f, 102.76f,
            1f, 412f, 102f,
            1f, 410.63f, 101.53f,
            1f, 409.2f, 101.37f,
            1f, 407.72f, 101.5f,
            1f, 406.21f, 101.93f,
            1f, 404.67f, 102.65f,
            1f, 403.12f, 103.66f,
            1f, 401.57f, 104.94f,
            1f, 400.02f, 106.47f,
            1f, 398.48f, 108.24f,
            1f, 396.97f, 110.23f,
            1f, 395.48f, 112.41f,
            1f, 394.04f, 114.77f,
            1f, 392.65f, 117.28f,
            1f, 391.32f, 119.91f,
            1f, 390.06f, 122.64f,
            1f, 388.88f, 125.43f,
            1f, 387.6f, 128.81f,
            1f, 386.47f, 132.26f,
            1f, 385.48f, 135.78f,
            1f, 384.62f, 139.35f,
            1f, 383.87f, 142.98f,
            1f, 383.23f, 146.65f,
            1f, 382.66f, 150.36f,
            1f, 382.14f, 154.09f,
            1f, 381.64f, 157.83f,
            1f, 381.14f, 161.57f,
            1f, 380.59f, 165.28f,
            1f, 379.94f, 168.95f,
            1f, 379.14f, 172.55f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 412f, 102f, 378f, 176f),
    ),
    // leafR2
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 348f, 178f,
            1f, 350.33f, 172.63f,
            1f, 353.12f, 167.39f,
            1f, 356.1f, 162.21f,
            1f, 359.2f, 157.06f,
            1f, 362.34f, 151.92f,
            1f, 365.47f, 146.78f,
            1f, 368.56f, 141.62f,
            1f, 371.55f, 136.44f,
            1f, 374.41f, 131.23f,
            1f, 377.12f, 125.96f,
            1f, 379.64f, 120.65f,
            1f, 381.95f, 115.27f,
            1f, 384.05f, 109.84f,
            1f, 385.93f, 104.34f,
            1f, 387.33f, 99.66f,
            1f, 388.58f, 94.99f,
            1f, 389.66f, 90.37f,
            1f, 390.56f, 85.84f,
            1f, 391.26f, 81.45f,
            1f, 391.75f, 77.23f,
            1f, 392.02f, 73.23f,
            1f, 392.07f, 69.47f,
            1f, 391.88f, 66.01f,
            1f, 391.46f, 62.86f,
            1f, 390.79f, 60.06f,
            1f, 389.89f, 57.62f,
            1f, 388.75f, 55.59f,
            1f, 387.38f, 53.96f,
            1f, 385.79f, 52.76f,
            1f, 384f, 52f,
            1f, 382.02f, 51.68f,
            1f, 379.87f, 51.82f,
            1f, 377.58f, 52.4f,
            1f, 375.17f, 53.42f,
            1f, 372.66f, 54.88f,
            1f, 370.09f, 56.75f,
            1f, 367.48f, 59.04f,
            1f, 364.87f, 61.7f,
            1f, 362.29f, 64.73f,
            1f, 359.77f, 68.09f,
            1f, 357.34f, 71.76f,
            1f, 355.03f, 75.69f,
            1f, 352.86f, 79.86f,
            1f, 350.86f, 84.22f,
            1f, 349.06f, 88.73f,
            1f, 347.46f, 93.35f,
            1f, 345.88f, 98.93f,
            1f, 344.65f, 104.62f,
            1f, 343.75f, 110.4f,
            1f, 343.18f, 116.27f,
            1f, 342.91f, 122.23f,
            1f, 342.92f, 128.27f,
            1f, 343.18f, 134.37f,
            1f, 343.66f, 140.55f,
            1f, 344.3f, 146.77f,
            1f, 345.07f, 153.02f,
            1f, 345.92f, 159.3f,
            1f, 346.78f, 165.58f,
            1f, 347.55f, 171.84f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 384f, 52f, 348f, 178f),
    ),
    // veinR2
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 350.7f, 162.42f,
            2f, 359.69f, 114.56f, 377.11f, 69.11f,
        ),
        stroke = ArtColor.VEIN,
        strokeWidth = 1.2f,
    ),
    // leafR6
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 470f, 210f,
            1f, 470.76f, 204.64f,
            1f, 471.7f, 199.27f,
            1f, 472.68f, 193.89f,
            1f, 473.63f, 188.51f,
            1f, 474.53f, 183.14f,
            1f, 475.35f, 177.78f,
            1f, 476.05f, 172.43f,
            1f, 476.62f, 167.09f,
            1f, 477.04f, 161.77f,
            1f, 477.29f, 156.46f,
            1f, 477.35f, 151.18f,
            1f, 477.21f, 145.91f,
            1f, 476.87f, 140.66f,
            1f, 476.32f, 135.44f,
            1f, 475.69f, 131.06f,
            1f, 474.94f, 126.74f,
            1f, 474.06f, 122.52f,
            1f, 473.07f, 118.43f,
            1f, 471.98f, 114.53f,
            1f, 470.8f, 110.83f,
            1f, 469.55f, 107.39f,
            1f, 468.25f, 104.23f,
            1f, 466.92f, 101.38f,
            1f, 465.56f, 98.87f,
            1f, 464.21f, 96.72f,
            1f, 462.87f, 94.95f,
            1f, 461.57f, 93.59f,
            1f, 460.31f, 92.64f,
            1f, 459.12f, 92.1f,
            1f, 458f, 92f,
            1f, 456.97f, 92.32f,
            1f, 456.04f, 93.07f,
            1f, 455.21f, 94.23f,
            1f, 454.49f, 95.81f,
            1f, 453.89f, 97.77f,
            1f, 453.4f, 100.1f,
            1f, 453.03f, 102.79f,
            1f, 452.78f, 105.8f,
            1f, 452.64f, 109.11f,
            1f, 452.6f, 112.68f,
            1f, 452.68f, 116.49f,
            1f, 452.85f, 120.49f,
            1f, 453.12f, 124.65f,
            1f, 453.47f, 128.93f,
            1f, 453.91f, 133.28f,
            1f, 454.43f, 137.67f,
            1f, 455.15f, 142.87f,
            1f, 455.98f, 148.07f,
            1f, 456.93f, 153.25f,
            1f, 457.98f, 158.43f,
            1f, 459.12f, 163.59f,
            1f, 460.33f, 168.74f,
            1f, 461.61f, 173.89f,
            1f, 462.93f, 179.04f,
            1f, 464.27f, 184.18f,
            1f, 465.6f, 189.33f,
            1f, 466.88f, 194.48f,
            1f, 468.1f, 199.63f,
            1f, 469.18f, 204.8f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 458f, 92f, 470f, 210f),
    ),
    // leafR4
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 468f, 216f,
            1f, 470.51f, 209.08f,
            1f, 473.5f, 202.3f,
            1f, 476.73f, 195.57f,
            1f, 480.08f, 188.87f,
            1f, 483.49f, 182.2f,
            1f, 486.91f, 175.52f,
            1f, 490.28f, 168.83f,
            1f, 493.57f, 162.12f,
            1f, 496.74f, 155.38f,
            1f, 499.75f, 148.59f,
            1f, 502.58f, 141.76f,
            1f, 505.21f, 134.88f,
            1f, 507.61f, 127.94f,
            1f, 509.79f, 120.94f,
            1f, 511.46f, 115f,
            1f, 512.97f, 109.09f,
            1f, 514.3f, 103.25f,
            1f, 515.44f, 97.55f,
            1f, 516.37f, 92.03f,
            1f, 517.08f, 86.74f,
            1f, 517.55f, 81.74f,
            1f, 517.77f, 77.08f,
            1f, 517.74f, 72.78f,
            1f, 517.44f, 68.9f,
            1f, 516.86f, 65.47f,
            1f, 516.01f, 62.53f,
            1f, 514.9f, 60.08f,
            1f, 513.52f, 58.17f,
            1f, 511.88f, 56.81f,
            1f, 510f, 56f,
            1f, 507.9f, 55.76f,
            1f, 505.59f, 56.09f,
            1f, 503.11f, 56.99f,
            1f, 500.47f, 58.45f,
            1f, 497.71f, 60.45f,
            1f, 494.87f, 62.98f,
            1f, 491.97f, 66.02f,
            1f, 489.05f, 69.54f,
            1f, 486.15f, 73.5f,
            1f, 483.3f, 77.88f,
            1f, 480.54f, 82.62f,
            1f, 477.91f, 87.7f,
            1f, 475.43f, 93.05f,
            1f, 473.12f, 98.63f,
            1f, 471.03f, 104.39f,
            1f, 469.17f, 110.27f,
            1f, 467.3f, 117.35f,
            1f, 465.8f, 124.54f,
            1f, 464.68f, 131.81f,
            1f, 463.9f, 139.19f,
            1f, 463.47f, 146.64f,
            1f, 463.34f, 154.18f,
            1f, 463.48f, 161.8f,
            1f, 463.86f, 169.47f,
            1f, 464.44f, 177.2f,
            1f, 465.16f, 184.96f,
            1f, 465.98f, 192.75f,
            1f, 466.81f, 200.54f,
            1f, 467.57f, 208.31f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 510f, 56f, 468f, 216f),
    ),
    // veinR4
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 471f, 196.26f,
            2f, 481.1f, 135.64f, 501.79f, 77.79f,
        ),
        stroke = ArtColor.VEIN,
        strokeWidth = 1.2f,
    ),
    // leafR5
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 462f, 218f,
            1f, 465.77f, 214.66f,
            1f, 469.89f, 211.67f,
            1f, 474.19f, 208.85f,
            1f, 478.6f, 206.12f,
            1f, 483.07f, 203.46f,
            1f, 487.55f, 200.8f,
            1f, 492.02f, 198.14f,
            1f, 496.44f, 195.42f,
            1f, 500.79f, 192.64f,
            1f, 505.04f, 189.77f,
            1f, 509.17f, 186.78f,
            1f, 513.18f, 183.67f,
            1f, 517.04f, 180.43f,
            1f, 520.75f, 177.05f,
            1f, 523.77f, 174.1f,
            1f, 526.66f, 171.09f,
            1f, 529.41f, 168.05f,
            1f, 531.97f, 165f,
            1f, 534.32f, 161.97f,
            1f, 536.44f, 158.99f,
            1f, 538.28f, 156.07f,
            1f, 539.85f, 153.24f,
            1f, 541.09f, 150.51f,
            1f, 542.01f, 147.92f,
            1f, 542.59f, 145.47f,
            1f, 542.8f, 143.18f,
            1f, 542.65f, 141.08f,
            1f, 542.13f, 139.17f,
            1f, 541.25f, 137.47f,
            1f, 540f, 136f,
            1f, 538.41f, 134.77f,
            1f, 536.48f, 133.79f,
            1f, 534.24f, 133.07f,
            1f, 531.71f, 132.63f,
            1f, 528.92f, 132.47f,
            1f, 525.91f, 132.6f,
            1f, 522.71f, 133.02f,
            1f, 519.35f, 133.74f,
            1f, 515.88f, 134.76f,
            1f, 512.34f, 136.07f,
            1f, 508.76f, 137.66f,
            1f, 505.19f, 139.53f,
            1f, 501.67f, 141.67f,
            1f, 498.24f, 144.05f,
            1f, 494.93f, 146.66f,
            1f, 491.77f, 149.48f,
            1f, 488.28f, 153.07f,
            1f, 485.06f, 156.93f,
            1f, 482.13f, 161.06f,
            1f, 479.47f, 165.44f,
            1f, 477.05f, 170.06f,
            1f, 474.87f, 174.91f,
            1f, 472.9f, 179.95f,
            1f, 471.11f, 185.16f,
            1f, 469.48f, 190.53f,
            1f, 467.96f, 196f,
            1f, 466.52f, 201.55f,
            1f, 465.12f, 207.13f,
            1f, 463.67f, 212.67f,
            4f,
        ),
        fill = ArtFill(ArtColor.LEAF_A, ArtColor.LEAF_B, 540f, 136f, 462f, 218f),
    ),
    // veinR5
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 469.52f, 206.41f,
            2f, 493.49f, 171.42f, 526.99f, 145.49f,
        ),
        stroke = ArtColor.VEIN,
        strokeWidth = 1.2f,
    ),
    // flap
    ArtShape(
        ArtLayer.Calendar,
        floatArrayOf(
            0f, 110.66f, 38.98f,
            2f, 111f, 36f, 111.57f, 38.94f,
            1f, 118f, 72f,
            1f, 118f, 72f,
            1f, 133.79f, 225.01f,
            2f, 134f, 227f, 132f, 227.1f,
            1f, 96.12f, 228.8f,
            2f, 92f, 229f, 91f, 225f,
            1f, 90.97f, 224.88f,
            2f, 90f, 221f, 90.45f, 217.03f,
            4f,
        ),
        fill = ArtFill(ArtColor.FLAP_TOP, ArtColor.FLAP_BOTTOM, 100f, 40f, 115f, 229f),
    ),
    // stack
    ArtShape(
        ArtLayer.Calendar,
        floatArrayOf(
            0f, 117f, 72f,
            1f, 117f, 72f,
            1f, 306f, 61f,
            1f, 306f, 61f,
            1f, 314.67f, 220.01f,
            2f, 315f, 226f, 309.02f, 226.45f,
            1f, 150.79f, 238.41f,
            2f, 143f, 239f, 137f, 234f,
            1f, 133.3f, 230.92f,
            2f, 131f, 229f, 130.73f, 226.01f,
            4f,
        ),
        fill = ArtFill(ArtColor.STACK),
    ),
    // header
    ArtShape(
        ArtLayer.Calendar,
        floatArrayOf(
            0f, 112.01f, 37.93f,
            2f, 111f, 31f, 117.99f, 30.71f,
            1f, 297.01f, 23.29f,
            2f, 304f, 23f, 304.37f, 29.99f,
            1f, 306f, 61f,
            1f, 306f, 61f,
            1f, 117f, 72f,
            1f, 117f, 72f,
            4f,
        ),
        fill = ArtFill(ArtColor.HEADER_TOP, ArtColor.HEADER_BOTTOM, 200f, 25f, 200f, 70f),
    ),
    // page
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 120f, 72.5f,
            1f, 120f, 72.5f,
            1f, 306f, 62.5f,
            1f, 306f, 62.5f,
            1f, 310.81f, 218f,
            2f, 311f, 224f, 305.01f, 224.32f,
            1f, 148.99f, 232.58f,
            2f, 141f, 233f, 139.96f, 225.07f,
            4f,
        ),
        fill = ArtFill(ArtColor.PAGE, ArtColor.PAGE_SHADE, 200f, 70f, 220f, 233f),
    ),
    // cell00
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 144.47f, 96.73f,
            2f, 143.89f, 91.77f, 148.88f, 91.49f,
            1f, 169.89f, 90.31f,
            2f, 174.89f, 90.03f, 175.47f, 95f,
            1f, 177.93f, 116.07f,
            2f, 178.51f, 121.03f, 173.52f, 121.31f,
            1f, 152.51f, 122.49f,
            2f, 147.51f, 122.77f, 146.93f, 117.8f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 149.2f, 91.4f, 173.2f, 121.4f),
    ),
    // cell01
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 182.67f, 94.58f,
            2f, 182.09f, 89.62f, 187.08f, 89.34f,
            1f, 208.09f, 88.16f,
            2f, 213.09f, 87.88f, 213.67f, 92.85f,
            1f, 216.13f, 113.92f,
            2f, 216.71f, 118.88f, 211.72f, 119.16f,
            1f, 190.71f, 120.34f,
            2f, 185.71f, 120.62f, 185.13f, 115.65f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 187.4f, 89.25f, 211.4f, 119.25f),
    ),
    // cell02
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 220.87f, 92.43f,
            2f, 220.29f, 87.47f, 225.28f, 87.19f,
            1f, 246.29f, 86.01f,
            2f, 251.29f, 85.73f, 251.87f, 90.7f,
            1f, 254.33f, 111.77f,
            2f, 254.91f, 116.73f, 249.92f, 117.01f,
            1f, 228.91f, 118.19f,
            2f, 223.91f, 118.47f, 223.33f, 113.5f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 225.6f, 87.1f, 249.6f, 117.1f),
    ),
    // cell03
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 259.07f, 90.28f,
            2f, 258.49f, 85.32f, 263.48f, 85.04f,
            1f, 284.49f, 83.86f,
            2f, 289.49f, 83.58f, 290.07f, 88.55f,
            1f, 292.53f, 109.62f,
            2f, 293.11f, 114.58f, 288.12f, 114.86f,
            1f, 267.11f, 116.04f,
            2f, 262.11f, 116.32f, 261.53f, 111.35f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 263.8f, 84.95f, 287.8f, 114.95f),
    ),
    // cell10
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 149.32f, 138.23f,
            2f, 148.74f, 133.27f, 153.73f, 132.99f,
            1f, 174.74f, 131.81f,
            2f, 179.74f, 131.53f, 180.32f, 136.5f,
            1f, 182.78f, 157.57f,
            2f, 183.36f, 162.53f, 178.37f, 162.81f,
            1f, 157.36f, 163.99f,
            2f, 152.36f, 164.27f, 151.78f, 159.3f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 154.05f, 132.9f, 178.05f, 162.9f),
    ),
    // cell11
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 187.52f, 136.08f,
            2f, 186.94f, 131.12f, 191.93f, 130.84f,
            1f, 212.94f, 129.66f,
            2f, 217.94f, 129.38f, 218.52f, 134.35f,
            1f, 220.98f, 155.42f,
            2f, 221.56f, 160.38f, 216.57f, 160.66f,
            1f, 195.56f, 161.84f,
            2f, 190.56f, 162.12f, 189.98f, 157.15f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 192.25f, 130.75f, 216.25f, 160.75f),
    ),
    // cell12
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 225.72f, 133.93f,
            2f, 225.14f, 128.97f, 230.13f, 128.69f,
            1f, 251.14f, 127.51f,
            2f, 256.14f, 127.23f, 256.72f, 132.2f,
            1f, 259.18f, 153.27f,
            2f, 259.76f, 158.23f, 254.77f, 158.51f,
            1f, 233.76f, 159.69f,
            2f, 228.76f, 159.97f, 228.18f, 155f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 230.45f, 128.6f, 254.45f, 158.6f),
    ),
    // cell13
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 263.92f, 131.78f,
            2f, 263.34f, 126.82f, 268.33f, 126.54f,
            1f, 289.34f, 125.36f,
            2f, 294.34f, 125.08f, 294.92f, 130.05f,
            1f, 297.38f, 151.12f,
            2f, 297.96f, 156.08f, 292.97f, 156.36f,
            1f, 271.96f, 157.54f,
            2f, 266.96f, 157.82f, 266.38f, 152.85f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 268.65f, 126.45f, 292.65f, 156.45f),
    ),
    // cell20
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 154.17f, 179.73f,
            2f, 153.59f, 174.77f, 158.58f, 174.49f,
            1f, 179.59f, 173.31f,
            2f, 184.59f, 173.03f, 185.17f, 178f,
            1f, 187.63f, 199.07f,
            2f, 188.21f, 204.03f, 183.22f, 204.31f,
            1f, 162.21f, 205.49f,
            2f, 157.21f, 205.77f, 156.63f, 200.8f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 158.9f, 174.4f, 182.9f, 204.4f),
    ),
    // cell21
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 192.37f, 177.58f,
            2f, 191.79f, 172.62f, 196.78f, 172.34f,
            1f, 217.79f, 171.16f,
            2f, 222.79f, 170.88f, 223.37f, 175.85f,
            1f, 225.83f, 196.92f,
            2f, 226.41f, 201.88f, 221.42f, 202.16f,
            1f, 200.41f, 203.34f,
            2f, 195.41f, 203.62f, 194.83f, 198.65f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 197.1f, 172.25f, 221.1f, 202.25f),
    ),
    // cell22
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 230.57f, 175.43f,
            2f, 229.99f, 170.47f, 234.98f, 170.19f,
            1f, 255.99f, 169.01f,
            2f, 260.99f, 168.73f, 261.57f, 173.7f,
            1f, 264.03f, 194.77f,
            2f, 264.61f, 199.73f, 259.62f, 200.01f,
            1f, 238.61f, 201.19f,
            2f, 233.61f, 201.47f, 233.03f, 196.5f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 235.3f, 170.1f, 259.3f, 200.1f),
    ),
    // cell23
    ArtShape(
        ArtLayer.Page,
        floatArrayOf(
            0f, 268.77f, 173.28f,
            2f, 268.19f, 168.32f, 273.18f, 168.04f,
            1f, 294.19f, 166.86f,
            2f, 299.19f, 166.58f, 299.77f, 171.55f,
            1f, 302.23f, 192.62f,
            2f, 302.81f, 197.58f, 297.82f, 197.86f,
            1f, 276.81f, 199.04f,
            2f, 271.81f, 199.32f, 271.23f, 194.35f,
            4f,
        ),
        fill = ArtFill(ArtColor.CELL_A, ArtColor.CELL_B, 273.5f, 167.95f, 297.5f, 197.95f),
    ),
    // ring
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 142.5f, 31f,
            1f, 142.5f, 19.5f,
            3f, 142.5f, 14.25f, 146.75f, 10f, 152f, 10f,
            3f, 157.25f, 10f, 161.5f, 14.25f, 161.5f, 19.5f,
            1f, 161.5f, 44f,
        ),
        stroke = ArtColor.RING,
        strokeWidth = 8.5f,
    ),
    // rivet
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 164.6f, 46f,
            3f, 164.6f, 49.65f, 161.65f, 52.6f, 158f, 52.6f,
            3f, 154.35f, 52.6f, 151.4f, 49.65f, 151.4f, 46f,
            3f, 151.4f, 42.35f, 154.35f, 39.4f, 158f, 39.4f,
            3f, 161.65f, 39.4f, 164.6f, 42.35f, 164.6f, 46f,
            4f,
        ),
        fill = ArtFill(ArtColor.RING_HI, ArtColor.RIVET, 153f, 41f, 163f, 51f),
    ),
    // ring
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 198.5f, 31f,
            1f, 198.5f, 16.5f,
            3f, 198.5f, 11.25f, 202.75f, 7f, 208f, 7f,
            3f, 213.25f, 7f, 217.5f, 11.25f, 217.5f, 16.5f,
            1f, 217.5f, 41.5f,
        ),
        stroke = ArtColor.RING,
        strokeWidth = 8.5f,
    ),
    // rivet
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 220.1f, 43.5f,
            3f, 220.1f, 47.15f, 217.15f, 50.1f, 213.5f, 50.1f,
            3f, 209.85f, 50.1f, 206.9f, 47.15f, 206.9f, 43.5f,
            3f, 206.9f, 39.85f, 209.85f, 36.9f, 213.5f, 36.9f,
            3f, 217.15f, 36.9f, 220.1f, 39.85f, 220.1f, 43.5f,
            4f,
        ),
        fill = ArtFill(ArtColor.RING_HI, ArtColor.RIVET, 208.5f, 38.5f, 218.5f, 48.5f),
    ),
    // ring
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 253.5f, 31f,
            1f, 253.5f, 14.5f,
            3f, 253.5f, 9.25f, 257.75f, 5f, 263f, 5f,
            3f, 268.25f, 5f, 272.5f, 9.25f, 272.5f, 14.5f,
            1f, 272.5f, 39f,
        ),
        stroke = ArtColor.RING,
        strokeWidth = 8.5f,
    ),
    // rivet
    ArtShape(
        ArtLayer.Rings,
        floatArrayOf(
            0f, 275.1f, 41f,
            3f, 275.1f, 44.65f, 272.15f, 47.6f, 268.5f, 47.6f,
            3f, 264.85f, 47.6f, 261.9f, 44.65f, 261.9f, 41f,
            3f, 261.9f, 37.35f, 264.85f, 34.4f, 268.5f, 34.4f,
            3f, 272.15f, 34.4f, 275.1f, 37.35f, 275.1f, 41f,
            4f,
        ),
        fill = ArtFill(ArtColor.RING_HI, ArtColor.RIVET, 263.5f, 36f, 273.5f, 46f),
    ),
    // coverTop
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 292.35f, 198.93f,
            2f, 293f, 196f, 296f, 196.03f,
            1f, 468.2f, 197.98f,
            2f, 470f, 198f, 471f, 199.5f,
            1f, 471f, 199.5f,
            2f, 472f, 201f, 470.2f, 201.1f,
            1f, 380f, 206f,
            1f, 380f, 206f,
            1f, 294f, 205.03f,
            2f, 291f, 205f, 291.65f, 202.07f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER_TOP),
    ),
    // spine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 295f, 197f,
            1f, 382f, 202f,
            1f, 382f, 236f,
            1f, 295f, 232f,
            3f, 280f, 232f, 280f, 197f, 295f, 197f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER, ArtColor.COVER_DARK, 283f, 196f, 283f, 232f),
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 293f, 199f,
            1f, 293f, 231f,
        ),
        stroke = ArtColor.SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 313f, 199f,
            1f, 313f, 231f,
        ),
        stroke = ArtColor.SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // pages
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382f, 206f,
            1f, 461f, 203f,
            1f, 461f, 230f,
            1f, 388f, 234f,
            3f, 379f, 234f, 379f, 208f, 382f, 206f,
            4f,
        ),
        fill = ArtFill(ArtColor.PAGES, ArtColor.PAGES_SHADE, 380f, 206f, 380f, 232f),
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 214.8f,
            1f, 457f, 211.9f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 222.6f,
            1f, 457f, 219.7f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // coverLip
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382.37f, 233.93f,
            2f, 382f, 233f, 383f, 232.95f,
            1f, 469f, 229.05f,
            2f, 470f, 229f, 470.55f, 229.83f,
            1f, 471.45f, 231.17f,
            2f, 472f, 232f, 471.29f, 232.71f,
            1f, 470.71f, 233.29f,
            2f, 470f, 234f, 469f, 234.05f,
            1f, 386f, 237.91f,
            2f, 384f, 238f, 383.26f, 236.14f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER_DARK),
    ),
    // coverTop
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 303.35f, 168.93f,
            2f, 304f, 166f, 307f, 166.04f,
            1f, 466.2f, 167.98f,
            2f, 468f, 168f, 469f, 169.5f,
            1f, 469f, 169.5f,
            2f, 470f, 171f, 468.2f, 171.1f,
            1f, 380f, 176f,
            1f, 380f, 176f,
            1f, 305f, 175.04f,
            2f, 302f, 175f, 302.65f, 172.07f,
            4f,
        ),
        fill = ArtFill(ArtColor.PALE_SPINE_HI),
    ),
    // spine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 306f, 167f,
            1f, 382f, 172f,
            1f, 382f, 203f,
            1f, 306f, 199f,
            3f, 291f, 199f, 291f, 167f, 306f, 167f,
            4f,
        ),
        fill = ArtFill(ArtColor.PALE_COVER, ArtColor.PALE_COVER_DARK, 294f, 166f, 294f, 199f),
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 304f, 169f,
            1f, 304f, 198f,
        ),
        stroke = ArtColor.PALE_SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 324f, 169f,
            1f, 324f, 198f,
        ),
        stroke = ArtColor.PALE_SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // pages
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382f, 176f,
            1f, 455f, 173f,
            1f, 455f, 197f,
            1f, 388f, 201f,
            3f, 379f, 201f, 379f, 178f, 382f, 176f,
            4f,
        ),
        fill = ArtFill(ArtColor.PAGES, ArtColor.PAGES_SHADE, 380f, 176f, 380f, 199f),
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 183.72f,
            1f, 451f, 180.91f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 190.74f,
            1f, 451f, 187.93f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // coverLip
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382.37f, 200.93f,
            2f, 382f, 200f, 383f, 199.95f,
            1f, 467f, 196.05f,
            2f, 468f, 196f, 468.55f, 196.83f,
            1f, 469.45f, 198.17f,
            2f, 470f, 199f, 469.29f, 199.71f,
            1f, 468.71f, 200.29f,
            2f, 468f, 201f, 467f, 201.05f,
            1f, 386f, 204.9f,
            2f, 384f, 205f, 383.26f, 203.14f,
            4f,
        ),
        fill = ArtFill(ArtColor.PALE_COVER_DARK),
    ),
    // coverTop
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 315.35f, 126.93f,
            2f, 316f, 124f, 319f, 124.04f,
            1f, 459.2f, 125.98f,
            2f, 461f, 126f, 462f, 127.5f,
            1f, 462f, 127.5f,
            2f, 463f, 129f, 461.2f, 129.11f,
            1f, 380f, 134f,
            1f, 380f, 134f,
            1f, 317f, 133.05f,
            2f, 314f, 133f, 314.65f, 130.07f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER_TOP),
    ),
    // spine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 318f, 125f,
            1f, 382f, 130f,
            1f, 382f, 164f,
            1f, 318f, 160f,
            3f, 303f, 160f, 303f, 125f, 318f, 125f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER, ArtColor.COVER_DARK, 306f, 124f, 306f, 160f),
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 316f, 127f,
            1f, 316f, 159f,
        ),
        stroke = ArtColor.SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // spineBand
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 336f, 127f,
            1f, 336f, 159f,
        ),
        stroke = ArtColor.SPINE_HI,
        strokeWidth = 2.6f,
    ),
    // pages
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382f, 134f,
            1f, 447f, 131f,
            1f, 447f, 158f,
            1f, 388f, 162f,
            3f, 379f, 162f, 379f, 136f, 382f, 134f,
            4f,
        ),
        fill = ArtFill(ArtColor.PAGES, ArtColor.PAGES_SHADE, 380f, 134f, 380f, 160f),
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 142.8f,
            1f, 443f, 139.9f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // pageLine
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 398f, 150.6f,
            1f, 443f, 147.7f,
        ),
        stroke = ArtColor.PAGE_LINE,
        strokeWidth = 0.9f,
    ),
    // coverLip
    ArtShape(
        ArtLayer.Books,
        floatArrayOf(
            0f, 382.37f, 161.93f,
            2f, 382f, 161f, 383f, 160.95f,
            1f, 460f, 157.05f,
            2f, 461f, 157f, 461.55f, 157.83f,
            1f, 462.45f, 159.17f,
            2f, 463f, 160f, 462.29f, 160.71f,
            1f, 461.71f, 161.29f,
            2f, 461f, 162f, 460f, 162.05f,
            1f, 386f, 165.9f,
            2f, 384f, 166f, 383.26f, 164.14f,
            4f,
        ),
        fill = ArtFill(ArtColor.COVER_DARK),
    ),
)

/** Логотип: 0 — верх квадрата, 1 — низ квадрата, 2 — шапочка. */
internal val LogoLight = intArrayOf(0xFF2A89EA.toInt(), 0xFF2F8EEE.toInt(), 0xFFFFFFFF.toInt())
internal val LogoDark = intArrayOf(0xFF2A84E2.toInt(), 0xFF3A93F0.toInt(), 0xFFFFFFFF.toInt())

internal val LogoShapes = listOf(
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 5f, 66f,
            3f, 5f, 30.01f, 30.01f, 5f, 66f, 5f,
            1f, 163f, 5f,
            3f, 198.99f, 5f, 224f, 30.01f, 224f, 66f,
            1f, 224f, 160f,
            3f, 224f, 195.99f, 198.99f, 221f, 163f, 221f,
            1f, 66f, 221f,
            3f, 30.01f, 221f, 5f, 195.99f, 5f, 160f,
            4f,
        ),
        fill = ArtFill(0, 1, 0f, 0f, 0f, 226f),
    ),
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 43.1f, 97.44f,
            2f, 37f, 94f, 43.1f, 90.56f,
            1f, 109.77f, 52.95f,
            2f, 115f, 50f, 120.25f, 52.9f,
            1f, 188.37f, 90.55f,
            2f, 191f, 92f, 191f, 95f,
            1f, 191f, 140.5f,
            2f, 191f, 147f, 184.5f, 147f,
            1f, 184.5f, 147f,
            2f, 178f, 147f, 178f, 140.5f,
            1f, 178f, 106f,
            2f, 178f, 105f, 177.11f, 105.46f,
            1f, 122.09f, 134.29f,
            2f, 115f, 138f, 108.03f, 134.07f,
            4f,
        ),
        fill = ArtFill(2),
    ),
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 65f, 127f,
            2f, 65f, 124f, 67.68f, 125.34f,
            1f, 109.63f, 146.32f,
            2f, 115f, 149f, 120.34f, 146.27f,
            1f, 161.33f, 125.36f,
            2f, 164f, 124f, 164f, 127f,
            1f, 164f, 146f,
            2f, 164f, 152f, 158.74f, 154.9f,
            1f, 122.88f, 174.66f,
            2f, 115f, 179f, 107.08f, 174.72f,
            1f, 70.28f, 154.85f,
            2f, 65f, 152f, 65f, 146f,
            4f,
        ),
        fill = ArtFill(2),
    ),
)

/** Фон и волны: 0 — фон экрана, 1–2 — пятно сверху, 3–4 — дальняя волна снизу, 5 — ближняя волна. */
internal val WavesLight = intArrayOf(0xFFF7FBFE.toInt(), 0xFFE6F1FD.toInt(), 0xFFEEF5FD.toInt(), 0xFFE2F0FD.toInt(), 0xFFE9F3FE.toInt(), 0xFFD2E7FD.toInt())
internal val WavesDark = intArrayOf(0xFF0E1522.toInt(), 0xFF14223A.toInt(), 0xFF111C30.toInt(), 0xFF13213A.toInt(), 0xFF121E34.toInt(), 0xFF172A48.toInt())

internal const val WAVE_TOP_HEIGHT = 372f
internal const val WAVE_BOTTOM_HEIGHT = 334f

/** Пятно в левом верхнем углу (от верха экрана). */
internal val WaveTop = listOf(
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, 351.8f, -40f,
            1f, -40f, -40f,
            1f, -40f, 362.5f,
            3f, 0.37f, 362.38f, -0.6f, 365.38f, 2.2f, 361.8f,
            3f, 5f, 358.22f, 8.92f, 355.55f, 16.8f, 341f,
            3f, 24.68f, 326.45f, 42.1f, 288.37f, 49.5f, 274.5f,
            3f, 56.9f, 260.63f, 56.48f, 263.52f, 61.2f, 257.8f,
            3f, 65.92f, 252.08f, 70.75f, 246.25f, 77.8f, 240.2f,
            3f, 84.85f, 234.15f, 96f, 226.33f, 103.5f, 221.5f,
            3f, 111f, 216.67f, 99.22f, 221.15f, 122.8f, 211.2f,
            3f, 146.38f, 201.25f, 218.83f, 173.63f, 245f, 161.8f,
            3f, 271.17f, 149.97f, 270.1f, 147.33f, 279.8f, 140.2f,
            3f, 289.5f, 133.07f, 296.5f, 126.17f, 303.2f, 119f,
            3f, 309.9f, 111.83f, 314.67f, 105.53f, 320f, 97.2f,
            3f, 325.33f, 88.87f, 330.9f, 79f, 335.2f, 69f,
            3f, 339.5f, 59f, 343.03f, 48.7f, 345.8f, 37.2f,
            3f, 348.57f, 25.7f, 350.8f, 6.2f, 351.8f, -40f,
            4f,
        ),
        fill = ArtFill(1, 2, 0f, 0f, 360f, 360f),
    ),
)

/** Волны внизу (y — от верха полосы высотой WAVE_BOTTOM_HEIGHT, прижатой к низу экрана). */
internal val WaveBottom = listOf(
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, -40f, 21f,
            1f, -40f, 374f,
            1f, 903f, 374f,
            1f, 903f, 105f,
            3f, 857.83f, 102.97f, 844.93f, 96.97f, 833f, 92.8f,
            3f, 821.07f, 88.63f, 805.5f, 83.3f, 791.2f, 80f,
            3f, 776.9f, 76.7f, 762.65f, 74.17f, 747.2f, 73f,
            3f, 731.75f, 71.83f, 712.45f, 72.17f, 698.5f, 73f,
            3f, 684.55f, 73.83f, 674.33f, 75.92f, 663.5f, 78f,
            3f, 652.67f, 80.08f, 643.28f, 82.58f, 633.5f, 85.5f,
            3f, 623.72f, 88.42f, 628.38f, 85.67f, 604.8f, 95.5f,
            3f, 581.22f, 105.33f, 520.1f, 133.12f, 492f, 144.5f,
            3f, 463.9f, 155.88f, 450.83f, 159.22f, 436.2f, 163.8f,
            3f, 421.57f, 168.38f, 414.37f, 169.8f, 404.2f, 172f,
            3f, 394.03f, 174.2f, 387.82f, 175.83f, 375.2f, 177f,
            3f, 362.58f, 178.17f, 342.12f, 179.58f, 328.5f, 179f,
            3f, 314.88f, 178.42f, 303.12f, 175.92f, 293.5f, 173.5f,
            3f, 283.88f, 171.08f, 276.63f, 167.22f, 270.8f, 164.5f,
            3f, 264.97f, 161.78f, 264.5f, 161.37f, 258.5f, 157.2f,
            3f, 252.5f, 153.03f, 247.38f, 150.62f, 234.8f, 139.5f,
            3f, 222.22f, 128.38f, 197f, 103.17f, 183f, 90.5f,
            3f, 169f, 77.83f, 160.63f, 70.92f, 150.8f, 63.5f,
            3f, 140.97f, 56.08f, 131.63f, 50.5f, 124f, 46f,
            3f, 116.37f, 41.5f, 111.8f, 39.42f, 105f, 36.5f,
            3f, 98.2f, 33.58f, 91.67f, 30.92f, 83.2f, 28.5f,
            3f, 74.73f, 26.08f, 62.87f, 23.42f, 54.2f, 22f,
            3f, 45.53f, 20.58f, 40.23f, 20.17f, 31.2f, 20f,
            3f, 22.17f, 19.83f, 5.2f, 20.83f, -40f, 21f,
            4f,
        ),
        fill = ArtFill(3, 4, 0f, 0f, 863f, 0f),
    ),
    ArtShape(
        ArtLayer.Back,
        floatArrayOf(
            0f, -40f, 159.2f,
            1f, -40f, 374f,
            1f, 849f, 374f,
            3f, 846f, 330.25f, 836.7f, 318.38f, 831f, 312.5f,
            3f, 825.3f, 306.62f, 821.27f, 303.45f, 814.8f, 298.5f,
            3f, 808.33f, 293.55f, 803.33f, 288.97f, 792.2f, 282.8f,
            3f, 781.07f, 276.63f, 761.5f, 266.8f, 748f, 261.5f,
            3f, 734.5f, 256.2f, 723.17f, 253.58f, 711.2f, 251f,
            3f, 699.23f, 248.42f, 688.98f, 246.83f, 676.2f, 246f,
            3f, 663.42f, 245.17f, 647.62f, 245.33f, 634.5f, 246f,
            3f, 621.38f, 246.67f, 613.88f, 247.67f, 597.5f, 250f,
            3f, 581.12f, 252.33f, 555.08f, 257.5f, 536.2f, 260f,
            3f, 517.32f, 262.5f, 498.48f, 264f, 484.2f, 265f,
            3f, 469.92f, 266f, 464.78f, 266.5f, 450.5f, 266f,
            3f, 436.22f, 265.5f, 415.5f, 264.42f, 398.5f, 262f,
            3f, 381.5f, 259.58f, 362.28f, 255.08f, 348.5f, 251.5f,
            3f, 334.72f, 247.92f, 326.08f, 244.33f, 315.8f, 240.5f,
            3f, 305.52f, 236.67f, 305.93f, 237.67f, 286.8f, 228.5f,
            3f, 267.67f, 219.33f, 224.6f, 196.17f, 201f, 185.5f,
            3f, 177.4f, 174.83f, 160.17f, 169.25f, 145.2f, 164.5f,
            3f, 130.23f, 159.75f, 121.7f, 158.75f, 111.2f, 157f,
            3f, 100.7f, 155.25f, 92.82f, 154.5f, 82.2f, 154f,
            3f, 71.58f, 153.5f, 61.2f, 153.13f, 47.5f, 154f,
            3f, 33.8f, 154.87f, 7.92f, 158.33f, -40f, 159.2f,
            4f,
        ),
        fill = ArtFill(5),
    ),
)
