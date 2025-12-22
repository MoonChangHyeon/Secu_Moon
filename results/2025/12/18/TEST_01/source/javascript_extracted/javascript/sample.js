(async () => {
    const importedModule  = await import('./module.js');
    let pathname = importedModule.pathNameExport();
    let styleBase = document.createElement( 'link' );
    styleBase.href = pathname.substring( 0, pathname.indexOf( 'docs' ) + 4 ) + '/prettify/prettify.css';
})();