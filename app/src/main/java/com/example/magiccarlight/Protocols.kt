package com.example.magiccarlight

object Protocols {
    interface Protocol { val name: String; fun rgb(r:Int,g:Int,b:Int):ByteArray; fun brightness(p:Int):ByteArray; fun power(on:Boolean):ByteArray }
    // Strongest candidate: the APK's E1 implementation plus the public ELK/MELK 9-byte frame format.
    object E1 : Protocol {
        override val name = "E1 / 7E"
        private fun f(cmd:Int,a:Int=0,b:Int=0,c:Int=0,d:Int=0)=byteArrayOf(0x7e,0x00,cmd.toByte(),a.toByte(),b.toByte(),c.toByte(),d.toByte(),0x00,0xef.toByte())
        override fun rgb(r:Int,g:Int,b:Int)=f(0x05,r and 255,g and 255,b and 255)
        override fun brightness(p:Int)=f(0x01,p.coerceIn(0,100))
        override fun power(on:Boolean)=f(0x04,if(on)0xf0 else 0,0,if(on)0xff else 0)
    }
    // Candidate derived from the distinct E2 assembler in the uploaded APK. Validate on hardware.
    object E2 : Protocol {
        override val name = "E2 / 8E"
        private fun f(cmd:Int,a:Int=0,b:Int=0,c:Int=0,d:Int=0)=byteArrayOf(0x8e.toByte(),0x00,cmd.toByte(),a.toByte(),b.toByte(),c.toByte(),d.toByte(),0x00,0xef.toByte())
        override fun rgb(r:Int,g:Int,b:Int)=f(0x05,r and 255,g and 255,b and 255)
        override fun brightness(p:Int)=f(0x01,p.coerceIn(0,100))
        override fun power(on:Boolean)=f(0x04,if(on)0xf0 else 0,0,if(on)0xff else 0)
    }
    // Candidate RS mode. The APK has a separate RS implementation and uses the same FFF0/FFF3 transport.
    object RS : Protocol {
        override val name = "RS / 8E"
        private fun f(cmd:Int,a:Int=0,b:Int=0,c:Int=0,d:Int=0)=byteArrayOf(0x8e.toByte(),0x00,cmd.toByte(),a.toByte(),b.toByte(),c.toByte(),d.toByte(),0x00,0xef.toByte())
        override fun rgb(r:Int,g:Int,b:Int)=f(0x05,r and 255,g and 255,b and 255)
        override fun brightness(p:Int)=f(0x01,p.coerceIn(0,100))
        override fun power(on:Boolean)=f(0x04,if(on)0xf0 else 0,0,if(on)0xff else 0)
    }
}
