
import Vue from 'vue';
import {VDataTable} from 'vuetify/es5/components/VDataTable'
import MixinPagination from './VDataTableMixinPaginationFirstLast.js'


export default {
    name: 'v-data-table-custom',
    mixins: [MixinPagination],
    extends: VDataTable,
    computed: {
    }
};

