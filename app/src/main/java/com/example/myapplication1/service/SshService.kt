package com.example.myapplication1.service

import com.example.myapplication1.data.SshConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.InputStream
import java.io.OutputStream
import java.security.Security

class SshService {
    private var client: SSHClient? = null
    private var session: Session? = null
    private var shell: Session.Shell? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    init {
        // Force register BouncyCastle at the top to override Android's limited defaults
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    suspend fun connect(connection: SshConnection, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            disconnect()
            
            // Custom Config to force Legacy algorithms if standard negotiation fails
            val config = DefaultConfig()
            
            val sshClient = SSHClient(config)
            sshClient.addHostKeyVerifier(PromiscuousVerifier())
            
            // Set aggressive timeouts
            sshClient.connectTimeout = 30000
            sshClient.timeout = 30000
            
            sshClient.connect(connection.host, connection.port)
            
            // Cisco often fails if we don't handle the transport properly
            // authPassword is a standard way, but we can try to force simple auth
            sshClient.authPassword(connection.username, password)
            
            val session = sshClient.startSession()
            
            // Try different PTY types if one fails
            try {
                session.allocatePTY("vt100", 80, 24, 0, 0, emptyMap())
            } catch (e: Exception) {
                // Fallback to default if vt100 is rejected
                session.allocateDefaultPTY()
            }
            
            val shell = session.startShell()
            
            this@SshService.client = sshClient
            this@SshService.session = session
            this@SshService.shell = shell
            this@SshService.inputStream = shell.inputStream
            this@SshService.outputStream = shell.outputStream
            
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide more specific error message for debugging
            val errorMsg = e.message ?: e.toString()
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun sendCommand(command: String) = withContext(Dispatchers.IO) {
        try {
            outputStream?.let {
                it.write((command + "\n").toByteArray())
                it.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            shell?.close()
            session?.close()
            client?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            shell = null
            session = null
            client = null
            inputStream = null
            outputStream = null
        }
    }
}
