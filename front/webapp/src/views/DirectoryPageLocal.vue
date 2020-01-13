<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">
        <v-container class="px-2 py-1">
            <v-layout class="mb-3" justify-start row>
                <v-card class="pa-3" style="width: 40%; min-width: 350px">
                    <v-flex class="mb-2 pr-2">
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Название
                            </v-flex>
                            <v-flex align-self-center md6>
                                {{ mainInfo.name }}
                            </v-flex>
                        </v-layout>
                    </v-flex>
                </v-card>
            </v-layout>
            <v-layout>
                <v-card class="pa-3" style="width: 100%">
                    <v-card-title>
                        <v-flex md6>
                            <v-text-field
                                    v-model="search"
                                    append-icon="search"
                                    label="Найти"
                                    single-line
                                    hide-details
                                    :disabled="!!mainInfo.disabledFind"
                                    style="width: 50%"
                                    v-if="dirType !== 'erdi'"
                            ></v-text-field>
                        </v-flex>
                        <v-spacer></v-spacer>
                        <v-btn color="primary" :disabled="!!mainInfo.disabledCreate" @click="createRecord">СОЗДАТЬ</v-btn>
                    </v-card-title>
                    <v-data-table-custom
                            :headers="headers"
                            :items="data"
                            :pagination.sync="pagination"
                            :total-items="total"
                            :loading="loadData"
                            :rows-per-page-items="pages"
                            class="mx-4"
                    >
                        <template v-slot:items="props">
                            <tr v-if="dirType === 'user_erdi'">
                                <td><router-link :to="{name: 'usererdiview', params: { user_erdi_id: props.item.id}}">{{ props.item.id | noData}}</router-link></td>
                                <td><router-link :to="{name: 'usererdiview', params: { user_erdi_id: props.item.id}}"> {{ props.item.name | noData}}</router-link></td>
                                <td>{{ props.item.subtype | noData}}</td>
                                <td class="text-xs-right">
                                    <v-icon class="icon-btn" color="primary" @click="deleteRecord(props.item)">delete</v-icon>
                                </td>
                            </tr>
                            <tr v-if="dirType === 'domain_masks'">
                                <!--td width="10%"><router-link :to="{name: 'domaim_masks_view', params: { id: props.item.id}}">{{ props.item.id | noData}}</router-link></td>
                                <td width="30%"><router-link :to="{name: 'domaim_masks_view', params: { id: props.item.id}}"> {{ props.item.domainMask | noData}}</router-link></td -->
                                <td width="10%">{{ props.item.id | noData}}</td>
                                <td width="30%">{{ props.item.domainMask | noData}}</td>
                                <td>{{ props.item.domainsNumber | noData}}</td>
                                <td class="text-xs-right">
                                    <v-icon class="icon-btn" disabled color="primary" @click="deleteRecord(props.item)">delete</v-icon>
                                </td>
                            </tr>
                            <!--tr v-if="dirType === 'domain_masks'">
                                <td width="10%"><router-link :to="{name: 'domain_masks', params: { id: props.item.id}}">{{ props.item.id | noData}}</router-link></td>
                                <td width="30%"><router-link :to="{name: 'domain_masks', params: { id: props.item.id}}"> {{ props.item.domainMask | noData}}</router-link></td>
                                <td>{{ props.item.domainsNumber | noData}}</td>
                                <td class="text-xs-right">
                                    <v-icon class="icon-btn" color="primary" @click="deleteRecord(props.item)">delete</v-icon>
                                </td>
                            </tr-->
                        </template>
                        <template v-slot:no-results>
                            <v-alert :value="true" color="warning" icon="warning">
                                По запросу "{{ search }}" ничего не найдено.
                            </v-alert>
                        </template>
                        <template v-slot:no-data v-if="!loadData">
                            <v-alert :value="true" color="warning" icon="warning">
                                Нет данных для отображения.
                            </v-alert>
                        </template>
                    </v-data-table-custom>
                </v-card>
            </v-layout>
        </v-container>
        <DialogDeleteRecord v-model="showDeleteDialog"
                            :record="chosenRecord"
                            :del-url="deleteUrl"
                            :field-id="mainInfo.fieldId"
                            :field-name="mainInfo.fieldName"
                            @refreshData="refreshData"
                            @success="onDeleteSuccess"></DialogDeleteRecord>
    </v-layout>
</template>

<script>
    import moment from 'moment';
    import DirUserErdiCard from '../components/DirUserErdiCard'
    import * as VueNotifications from "vue-notifications";
    import DialogDeleteRecord from "../components/DialogDeleteRecord";

    export default {
        name: "DirectoryPageLocal",
        components: {DialogDeleteRecord, DirUserErdiCard},

        data() {
            return {
                dirType: null,

                pagination: {
                    sortBy: 'id',
                },
                search: null,
                pages: [10, 25, 50, 100],
                page: null,
                rowsPerPage: null,
                sortBy: null,
                descending: null,

                total: 0,
                params: [],

                mainInfo: {},
                headers: [],
                data: [],

                loadData: false,

                showCardDialog: false,
                showDeleteDialog: false,

                chosenRecord: null,
                deleteUrl: null
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showInfoMsg: {
                type: VueNotifications.types.info,
                title: '',
                message: ''
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.getData();
                    this.page = v.page-1;
                    this.rowsPerPage = v.rowsPerPage;
                    this.sortBy = v.sortBy;
                    this.descending = v.descending;
                },
                deep: true
            },

            search() {
                this.pagination.page = 1;
                this.getData();
            },

            dirType(v) {
                console.log(`type is ${v}`);
                switch (v) {
                    case 'user_erdi':
                        this.headers = [
                            {text: 'ID', value: 'id', sortable: false},
                            {text: 'Название', value: 'name', sortable: false},
                            {text: 'Тип нарушения', value: 'subtype', sortable: false},
                            {text: 'Действия', value: 'actions', align: 'right', sortable: false}
                        ];
                        this.mainInfo = {
                            name: "Пользовательские ЕРДИ",
                            delUrl: (record) => {
                                return this.$urls.CUSTOM_ERDI + "/" + record.id;
                            },
                            urlInfo: this.$urls.CUSTOM_ERDI,
                            simpleCreateFunction: () => {
                                this.$router.push({name: 'newusererdi'});
                            },
                            fieldId: "id",
                            fieldName: "name"
                        };
                        break;

                    case 'domain_masks':
                        this.headers = [
                            {text: 'ID', value: 'id', sortable: true},
                            {text: 'Маска', value: 'domainMask', sortable: true},
                            {text: 'Кол-во доменов', value: 'domainsNumber', sortable: false},
                            {text: 'Действия', value: 'actions', align: 'right', sortable: false}
                        ];
                        this.mainInfo = {
                            name: "Доменные маски",
                            delUrl: (record) => this.$urls.DOMAIN_MASKS_ACTION + "?id=" + record.id,
                            urlInfo: this.$urls.DOMAIN_MASKS_ALL,
                            simpleCreateFunction: () => {
                                this.$router.push({name: 'domain_masks', params: {id: "new"}});
                            },
                            disabledCreate: true,
                            disabledFind: true,
                            fieldId: "id",
                            fieldName: "domainMask"
                        };
                        break;

                    default:
                        this.mainInfo = {
                            name: 'Неизвестно'
                        };
                        break;
                }
                this.getData();
            }
        },

        computed: {
            today() {
                return moment().locale('ru').format('hh:mm DD.MM.YYYY');
            },
        },

        mounted() {
            this.dirType = this.$route.params.dir_type.toLowerCase();
        },

        updated() {
            this.dirType = this.$route.params.dir_type.toLowerCase();
        },

        methods: {

            getData() {
                const [pageNumber, pageSize, sortingColumn, sortingDirection] =
                    [this.pagination.page - 1, this.pagination.rowsPerPage, this.pagination.sortBy, this.pagination.descending];

                this.params = [];
                const config = () => ({
                    params: {
                        pageSize: pageSize,
                        pageNumber: pageNumber,
                        sortingColumn: sortingColumn,
                        sortingDirection: this.sortTable(sortingDirection),
                        query: this.search||''
                    }
                });

                if (this.mainInfo.urlInfo) {
                    this.getDataRequest(this.mainInfo.urlInfo, config);
                }
            },

            getDataRequest(url, config) {
                this.loadData = true;

                //пытаемся взять новые данные, пока не получим код 200
                this.$axios.get(url, config())
                    .then(resp => {
                        if (resp.status !== 200) {
                            setTimeout(this.getDataRequest(url, config), 4000);
                        }
                        else {
                            this.data = resp.data.content;
                            this.total = resp.data.totalElements;
                            this.pagination.totalItems = resp.data.totalElements;
                        }
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() =>{
                        this.loadData = false;
                    });
            },

            createRecord() {
                if (this.mainInfo.simpleCreateFunction){
                    this.mainInfo.simpleCreateFunction();
                }
                switch (this.dirType) {
                    default: break;
                }
            },

            deleteRecord(record) {
                this.chosenRecord = record;
                this.deleteUrl = this.mainInfo.delUrl(record);
                this.showDeleteDialog = true;
            },

            onDeleteSuccess(record){
                this.getData();
            },

            refreshData() {
                this.pagination.page = 1;
                this.getData();
            },

            sortTable(descending) {
                if (descending != null) {
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                } else return descending;
            },

            showCard(record) {
                this.chosenRecord = record;
                this.showCardDialog = true;
            },

            dateFormat(date) {
                if (date == null)
                    return 'Неизвестно';
                else {
                    return moment(date).locale('ru').format('hh:mm DD.MM.YYYY');
                }
            },

            dateTableFormat(date) {
                /*                if (date == null)
                                    return date;
                                else {
                                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                                }*/
                try {
                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                }
                catch {
                    return date;
                }
            },

            statusFormat(s) {
                return s ? 'Работает' : 'Не работает'
            }
        }
    }
</script>

<style scoped>
    .text-style {
        text-align: center;
    }

    .lineText {
        width: 100%;
        height: 45px;
    }

</style>
<style>
    .icon-btn {
        padding: 3px !important;
        height: auto !important;
        cursor: pointer;
    }
    .icon-btn.v-btn {
        min-width: 0 !important;
    }
    .icon-btn .v-icon {
        font-size: 30px;
    }
</style>