/**
 * Codespointer plugin perform as an IDEA action, it generates a summary of
 * the current editor selection to clipboard, optionally including various
 * information which is configurable with a live template.
 * 
 * @actiongroup Find the action at Toolbar -> Tools -> Create code pointers...
 *
 * @pluginScript This script should be registered as a plugin script with run
 *  mode 'While Intellij Started'.  
 *
 * @template The plugin will seek for a live template with name 'codespointer',
 * in the 'user' group, where the template syntax is described by 'trimpath template'
 * format.(http://code.google.com/p/trimpath/wiki/JavaScriptTemplateSyntax).
 *
 * Current the following variable names is available:
 * 1. filePath - The path of current document file in VCS, otherwise the project
 *  path is reported.
 * 2. startLine/endLine - The line numbers respectively.
 * 3. selectedText - The selected text content within current document.
 *
 * @example Below is an example of a simple Codespointer template:
 * 
 * ${filePath}#L${startLine}{if endLine} - L${endLine}{/if}
 * {if selectedText}
 * {{{
 * ${selectedText}
 * }}}
 * {/if}
 */
( function()
{
	var namespace = new JavaImporter(
			 java.lang.System,
			 java.awt.datatransfer,
			 java.io.File,
			 com.intellij.openapi.ide,
			 com.intellij.openapi.application,
			 com.intellij.openapi.project,
			 com.intellij.openapi.wm,
			 com.intellij.openapi.actionSystem,
			 com.intellij.openapi.fileEditor,
			 com.intellij.openapi.roots,
			 com.intellij.openapi.vfs,
			 com.intellij.openapi.vcs,
			 com.intellij.codeInsight.template.impl
			);

	with ( namespace ) {

		// Loading library files from 'lib' folder within scriptMonkey home.
		( function ( libName )
		{
			// TODO: Load from scriptmonkey 'home' configuration.
			var jsFolder = new File( new File( System.getProperty("user.home"), "scriptMonkey"), "js" ),
				libFile = new File( jsFolder, 'lib/' + libName + '.js' );
			load( libFile.getPath() );
		} )( 'trimpath' );

		function doAction( actionEvent )
		{
			// Virtual file project path.
			function getFilePathInProject()
			{
				var projectFileIndex = ProjectRootManager.getInstance( project ).getFileIndex();
				var root = projectFileIndex.getSourceRootForFile( file )
							  || projectFileIndex.getContentRootForFile(file);
				return String( VfsUtil.getRelativePath( file, root, '.' ) )
						.replace( /(\.)([^.]+(?=\.))/g, '/$2' );
			}

			// Virturl file VCS path.
			function getFilePathInSvn()
			{
				var vcsManager = ProjectLevelVcsManager.getInstance( project ),
						vcs = vcsManager.findVcsByName( 'svn' ),
						fileUrlMapping = vcs.getSvnFileUrlMapping();
				return String( fileUrlMapping.getUrlForFile( new File( file.getPath() ) ) || '' );
			}

			var project = actionEvent.getDataContext().getData( DataConstants.PROJECT ),
				editor = FileEditorManager.getInstance( project ).getSelectedTextEditor(),
				document = editor.getDocument(),
				file = FileDocumentManager.getInstance().getFile( document ),
				selModel = editor.getSelectionModel();

			// Convert package name based presentation of path to file path.
			var filePath = getFilePathInSvn() || getFilePathInProject(),
				// Retrieve line number info from selection model.
				lineNumber = {
				 "start" : document.getLineNumber( selModel.getSelectionStart() ),
				 "end" : document.getLineNumber( selModel.getSelectionEnd() )
				},
				selectedText = String( selModel.getSelectedText() || '' );

			// Compose the code pointer from template with key 'codespointer'.
			var tplSetting = TemplateSettings.getInstance(),
					tpl = tplSetting.getTemplate( 'codespointer', 'user' ),
				tplContent = tpl && String( tpl.getString() );

			if ( !tplContent )
			{
				alert( 'Live Template with name "codespointer" is required!' );
				return;
			}

			var contents = tplContent.process( { 'selectedText' : selectedText,
				'filePath' : filePath,
				'startLine' : lineNumber.start,
				'endLine' : lineNumber.end } );

			// Send the composed contents to clipboard.
			CopyPasteManager.getInstance().setContents( new StringSelection( contents ) );
			// TODO: Update status bar for prompting the success.
		}

		// Create an 'com.intellij.openapi.actionSystem.AnAction' instance
		// through action creation delegate API.
		var codesPointerAction = intellij.createAction( plugin,
				"ScriptMonkey.MainMenu.Tools.CodesPointers", "Create codes pointer...",
		{
			actionPerformed : doAction
		} );

		// Adding the action as the last entry of 'ToolsMenu' group.
		var actionManager = ActionManager.instance;
		var toolsMenu = actionManager.getAction( 'ToolsMenu' );
		toolsMenu.add( codesPointerAction, new Constraints( Anchor.BEFORE, Anchor.LAST ) );
	}
} )();
