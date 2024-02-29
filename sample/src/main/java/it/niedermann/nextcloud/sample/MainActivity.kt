package it.niedermann.nextcloud.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.android.markdown.MarkdownController
import it.niedermann.android.markdown.MarkdownEditor
import it.niedermann.android.markdown.MarkdownEditorImpl
import it.niedermann.android.markdown.MarkdownViewerImpl
import it.niedermann.nextcloud.exception.getDebugInfos

class MainActivity : AppCompatActivity() {

    private lateinit var signOn: Button
    private lateinit var currentUser: TextView
    private lateinit var sampleException: TextView
    private lateinit var toolbar: MarkdownController
    private lateinit var markdownEditor: MarkdownEditorImpl
    private lateinit var markdownViewer: MarkdownEditor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signOn = findViewById<MaterialButton>(R.id.signOn)
        currentUser = findViewById(R.id.currentUser)
        sampleException = findViewById(R.id.sampleException)
        toolbar = findViewById(R.id.toolbar)
        markdownEditor = findViewById(R.id.markdown_editor)
        markdownViewer = findViewById<MarkdownViewerImpl>(R.id.markdown_viewer)

        signOn.setOnClickListener {
            AccountImporter.pickNewAccount(this)
        }

        sampleException.text = getDebugInfos(this, RuntimeException())

        markdownEditor.registerController(toolbar)
        markdownEditor.setMarkdownStringChangedListener { str ->
            markdownViewer.setMarkdownString(str)
        }
        markdownEditor.setMarkdownString(getString(R.string.markdown_content))

        markdownEditor.setSearchText("and", 2)
        markdownViewer.setSearchText("and", 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AccountImporter.onActivityResult(requestCode, resultCode, data, this) {
            SingleAccountHelper.commitCurrentAccount(this, it.name)
            currentUser.text = it.name
            currentUser.visibility = View.VISIBLE
            markdownEditor.setCurrentSingleSignOnAccount(it, Color.RED)
            markdownViewer.setCurrentSingleSignOnAccount(it, Color.RED)
        }
    }
}