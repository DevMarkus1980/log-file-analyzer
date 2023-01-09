        var minDate, maxDate;

        // Custom filtering function which will search data in column four between two values
        $.fn.dataTable.ext.search.push(
            function( settings, data, dataIndex ) {
                var min = minDate.val();
                var max = maxDate.val();
                var date = new Date( data[1] );

                if (
                    ( min === null && max === null ) ||
                    ( min === null && date <= max ) ||
                    ( min <= date   && max === null ) ||
                    ( min <= date   && date <= max )
                ) {
                    return true;
                }
                return false;
            }
        );

        $(document).ready(function() {

         // Create date inputs
            minDate = new DateTime($('#min'), {
                format: 'YYYY-MM-DD HH:mm:ss'
            });

            maxDate = new DateTime($('#max'), {
                format: 'YYYY-MM-DD HH:mm:ss'
                });



            var table =
            $('#dataTable').DataTable({
                "dom" : 'Pfrtip',
                "searchPanes": {
                    "threshold" : 1,
                    "columns": [ 0,1,2,3 ],
                    "cascadePanes" : true,
                    "initCollapsed": true,
                    "viewTotal" : true
                    },
                columnDefs: [ {
                      targets: 1,
                    } ],

                 rowReorder: {
                    selector: 'td:nth-child(2)'
                },
                responsive: true
            });

            $('#min, #max').on('change', function () {
                table.draw();
                });


        });




