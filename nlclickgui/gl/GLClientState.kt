package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl


enum class GLClientState(val glName: String, override val cap: Int) : GLenum {
    COLOR("GL_COLOR_ARRAY", '\8076'.code),
    EDGE("GL_EDGE_FLAG_ARRAY", '\8079'.code),
    FOG("GL_FOG_COORD_ARRAY", '\8457'.code),
    INDEX("GL_INDEX_ARRAY", '\8077'.code),
    NORMAL("GL_NORMAL_ARRAY", '\8075'.code),
    SECONDARY_COLOR("GL_SECONDARY_COLOR_ARRAY", '\845e'.code),
    TEXTURE("GL_TEXTURE_COORD_ARRAY", '\8078'.code),
    VERTEX("GL_VERTEX_ARRAY", '\8074'.code);
}
