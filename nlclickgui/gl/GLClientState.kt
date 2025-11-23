package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl

// Fix: Renomeado 'name' para 'glName' para evitar conflito com a propriedade final 'Enum.name'
enum class GLClientState(val glName: String, override val cap: Int) : GLenum {
    COLOR("GL_COLOR_ARRAY", '\u8076'.code),
    EDGE("GL_EDGE_FLAG_ARRAY", '\u8079'.code),
    FOG("GL_FOG_COORD_ARRAY", '\u8457'.code),
    INDEX("GL_INDEX_ARRAY", '\u8077'.code),
    NORMAL("GL_NORMAL_ARRAY", '\u8075'.code),
    SECONDARY_COLOR("GL_SECONDARY_COLOR_ARRAY", '\u845e'.code),
    TEXTURE("GL_TEXTURE_COORD_ARRAY", '\u8078'.code),
    VERTEX("GL_VERTEX_ARRAY", '\u8074'.code);
}