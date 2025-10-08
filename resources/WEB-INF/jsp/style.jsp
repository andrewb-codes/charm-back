<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">

<style>
    :root{
      /* spacing */
      --space-1: 8px;
      --space-2: 16px;
      --space-3: 24px;

      /* icon tokens */
      --icon-sm: 20px;
      --icon-md: 28px;
      --icon-lg: 36px;
      --icon-header: 44px;    /* ← размер иконок в шапке (сердце + флаги) */

      /* layout */
      --max-w: 980px;
      --border: #ddd;
    }

    /* base */
    html, body { margin: 0; padding: 0; }
    body { font-family: "Roboto", system-ui, -apple-system, Segoe UI, Arial, sans-serif; }

    /* centered container */
    .container { max-width: var(--max-w); margin: 0 auto; padding: 0 var(--space-2); }

    /* flex helpers */
    .row { display: flex; align-items: center; }
    .row.center { justify-content: center; }
    .row.between { justify-content: space-between; }
    .cluster { display: flex; align-items: center; gap: var(--space-2); }

    /* header */
    .header-bar{
      display: flex;
      align-items: center;
      justify-content: center;
      gap: var(--space-3);
      line-height: 0;
    }
    .header-bar > *{ display:flex; align-items:center; margin:0; }
    .header-bar img{
      display: block;
      height: var(--icon-header);
      width: auto;
      object-fit: contain;
    }

    /* tables (centered inside container) */
    table { margin: 0 auto; border-collapse: collapse; width: 100%; max-width: var(--max-w); }
    tr { border-bottom: 1px solid var(--border); }
    td { padding: 10px; vertical-align: middle; }

    /* optional: for form-like tables give wider label column */
    .table--form td:first-child { width: 220px; font-weight: 600; }

    /* inputs */
    input, select, textarea {
      width: 100%;
      max-width: 500px;
      font: inherit;
      box-sizing: border-box;
    }
    textarea { resize: none; }

    /* icons/buttons */
    .btn-reset { background: none; border: 0; padding: 0; cursor: pointer; line-height: 0; }
    .icon-sm { width: var(--icon-sm); height: var(--icon-sm); display:block; }
    .icon-md { width: var(--icon-md); height: var(--icon-md); display:block; }
    .icon-lg { width: var(--icon-lg); height: var(--icon-lg); display:block; }
    .icon { width: 75px; display:block; } /* legacy */

    /* utilities */
    .divider { border: 0; border-top: 1px solid var(--border); margin: var(--space-3) 0; }
    .mt-1 { margin-top: var(--space-1); }
    .mt-2 { margin-top: var(--space-2); }
    .mt-3 { margin-top: var(--space-3); }
    .center-text { text-align: center; }
    .warning { color: #c62828; text-align: center; }

    /* footer */
    .footer { text-align: center; }
</style>