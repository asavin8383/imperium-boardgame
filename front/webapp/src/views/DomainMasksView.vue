<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">

        <v-container class="px-2 py-1 tasks-container">
            <v-layout align-start justify-space-between row class="mb-3">
                <v-flex md3 style="cursor: pointer" @click="onClickBack">
                    <v-layout row class="mt-2">
                        <v-icon color="black" class="mr-2">arrow_back</v-icon>
                        <span class="subheading">Назад</span>
                    </v-layout>
                </v-flex>
            </v-layout>

            <v-layout class="mb-3" justify-start row>
                <v-card class="record-card pa-3" style="width: 100%">
                    <v-card-title class="font-weight-bold pa-0 pb-4">
                        <v-flex align-self-center md6 style="font-size: larger">
                        </v-flex>
                        <v-spacer></v-spacer>
                    </v-card-title>
                    <v-card-text class="pa-0">
                        <v-layout v-if="!modeCreate" row class="pa-0 mb-2 lineText">
                            <v-flex md2 xs4 class="name-font">
                                ID Маски
                            </v-flex>
                            <v-flex md8 xs8 class="value-font">
                                {{ id | noData }}
                            </v-flex>
                        </v-layout>
                        <v-layout row class="pa-0 mb-2 lineText">
                            <v-flex md2 xs4 class="name-font">
                                <span>Доменная маска</span>
                            </v-flex>
                            <v-flex md8 xs8 class="value-font">
                                <span v-if="modeView">{{ viewData.domainMask | noData }}</span>
                                <v-text-field v-if="!modeView" v-model="viewData.domainMask"
                                              class="custom-text-field">
                                </v-text-field>
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>

                        <v-layout column v-if="modeCreate">
                            <v-layout row class="pa-0 my-2" justify-space-between>
                                <h3 class="ml-2">Список новых доменов</h3>
                                <v-btn v-if="modeCreate" color="primary" flat @click="openCreateUnitDialog">Добавить запись</v-btn>
                            </v-layout>

                            <v-layout row class="pa-0 mb-2">
                                <v-flex md12 >
                                    <v-data-table
                                            :headers="headers"
                                            :items="unitsNew"
                                            class="mx-4"
                                            hide-actions
                                    >
                                        <template v-slot:items="props">
                                            <td>{{ props.item.type | noData}}</td>
                                            <td>{{ props.item.domain | noData}}</td>
                                            <template v-if="modeCreate">
                                                <td class="text-xs-right">
                                                        <v-icon class="icon-btn" color="primary" @click="openEditUnitDialog(props.item)" >edit</v-icon>
                                                        <v-icon class="icon-btn" color="primary" @click="deleteUnit(props.item)">delete</v-icon>
                                                </td>
                                            </template>
                                        </template>
                                        <template v-slot:no-data v-if="!loadData">
                                            <v-alert :value="true" color="warning" icon="warning">
                                                Нет записей для отображения.
                                            </v-alert>
                                        </template>
                                    </v-data-table>
                                </v-flex>
                            </v-layout>
                        </v-layout>


                        <v-layout column v-if="!modeCreate">
                            <v-layout row class="pa-0 my-2" justify-space-between>
                                <h3 class="ml-2">Список доменов</h3>
                                <v-btn color="primary" flat @click="openCreateUnitDialog">Добавить запись</v-btn>
                            </v-layout>

                            <v-layout row class="pa-0 mb-2">
                                <v-flex md12 >
                                    <v-data-table-custom
                                            :headers="headers"
                                            :items="units.data"
                                            :pagination.sync="units.pagination"
                                            :total-items="units.total"
                                            :loading="loadData"
                                            :rows-per-page-items="units.pages"
                                            class="mx-4"
                                    >
                                        <template v-slot:items="props">
                                            <tr>
                                                <td width="200">{{ props.item.id | noData}}</td>
                                                <td>{{ props.item.domain | noData}}</td>
                                                <td class="text-xs-right">
                                                    <v-icon class="icon-btn" color="primary" @click="deleteUnit(props.item)">delete</v-icon>
                                                </td>
                                            </tr>
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
                                </v-flex>
                            </v-layout>
                        </v-layout>

                     </v-card-text>
                    <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-btn v-if="modeCreate" color="primary" @click="create" :disabled="!isValid">Создать</v-btn>
                    </v-card-actions>
                </v-card>
            </v-layout>
        </v-container>

        <dialog-domain v-model="dialogDomainShow" :unit="chosenUnit" @updateUnit="updateUnit" :action=action></dialog-domain>
    </v-layout>
</template>

<script>
    import DialogDomain from "../components/DialogDomain";
    import * as VueNotifications from "vue-notifications";

    const MODE = {CREATE: "CREATE", EDIT: "EDIT", VIEW: "VIEW"};
    
    export default {
        components: {DialogDomain},

        props: [],

        data() {
            return {
                title: null,
                loadingRequestCounter: 0,

                id: null,
                viewData: {},
                unitsNew: [],

                unitCounter: 0,

                units: {
                    data: [],
                    pagination: {
                        sortBy: 'id',
                    },
                    pages: [10, 25, 50, 100],
                    total: 0
                },

                mode: MODE.VIEW,

                dialogUnitDelete: false,
                dialogDomainShow: false,

                chosenUnit: {},
                action: null
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
            showWarnMsg: {
                type: VueNotifications.types.warn,
                title: '',
                message: '',
            },
        },

        computed: {
            modeCreate(){
                return this.mode === MODE.CREATE;
            },
            modeView(){
                return this.mode === MODE.VIEW;
            },
            
            headers() {
                return [
                    {text: 'ID', value: 'id', sortable: false},
                    {text: 'Домен', value: 'domain', sortable: false},
                    {text: 'Действия', value: 'actions', align: 'right', sortable: false}
                ]
            },

            isValid() {
                return !!this.viewData.domainMask;
            },

            loadData(){
                return this.loadingRequestCounter > 0;
            }
        },

        mounted() {
            this.initWithId(this.$route.params.id);
        },

        watch: {
            viewData(v){
                this.getData();
            },

            'units.pagination': {
                handler(v) {
                    this.getData();
                },
                deep: true
            },
        },

        methods: {
            initWithId(id){
                this.id = id;
                if (this.id != null && this.id !== "new") {
                    this.showUserErdi();
                } else {
                    this.createUserErdi();
                }
                this.$store.commit('setSection', `${this.title}`);
            },

            showUserErdi() {
                this.$router.replace({name: this.$router.currentRoute.name, params: {id: this.id}})
                this.title = `Пользовательское ЕРДИ # ${this.id}`;
                this.mode = MODE.VIEW;
                this.getInfo();
            },

            createUserErdi() {
                this.title = `Создание нового пользовательского ЕРДИ`;
                this.mode = MODE.CREATE;
                this.unitsNew = [];
                this.viewData = {
                    domainMask: null
                };
                this.unitsNew = [];
            },

            getInfo() {
                const params = {
                    id: this.id,
                };

                this.loadingRequestCounter++;

                this.$axios.get(this.$urls.DOMAIN_MASKS_INFO, {params}).then(resp => {
                    this.viewData = {
                        domainMask: resp.data.domainMask
                    };

                    this.unitsNew = resp.data.unitsNew;
                    //notification
                }).catch(e => {
                    console.log('error', e);
                })
                .finally(() => {
                    this.loadingRequestCounter--;
                });
            },

            openDeleteUnitDialog(unit) {
                this.chosenUnit = unit;
                this.dialogUnitDelete = true;
            },

            closeDeleteUnitDelete() {
                this.dialogUnitDelete = false;
            },

            deleteUnit(unit) {
                if (this.modeCreate){
                    this.unitsNew = this.unitsNew.filter(item => {
                        return item.id !== unit.id;
                    });
                    this.dialogUnitDelete = false;
                }
                else if (this.modeView){
                    this.$confirm(`Вы уверены, что хотите удалить домен №${unit.id}: '${unit.domain}'?`,
                        {title: "Внимание!", buttonTrueText: "Да", buttonFalseText: "Нет"})
                        .then(res => {
                            if (res)
                                this.confirmDeleteUnit(unit);
                        })
                }
            },

            confirmDeleteUnit(unit){
                this.$axios.delete(this.$urls.DOMAIN_MASKS_DELETE_DOMAIN, {params: {id: unit.id}})
                    .then(resp => {
                        this.showSuccessMsg({message: "Запись успешно удалена"});
                        this.getData();
                    })
                    .catch(e => {
                        console.log('error ', e);
                        this.showErrorMsg({message: "Ошибка при удалении домена!"});
                    });
            },

            openEditUnitDialog(unit) {
                this.chosenUnit = unit;
                this.action = 'edit';
                this.dialogDomainShow = true;
            },

            openCreateUnitDialog() {
                this.chosenUnit = {};
                this.action = 'create';
                this.dialogDomainShow = true;
            },

            getData() {
                if (!this.viewData.domainMask)
                    return;

                const [pageNumber, pageSize, sortingColumn, sortingDirection] =
                    [this.units.pagination.page - 1, this.units.pagination.rowsPerPage, this.units.pagination.sortBy, this.units.pagination.descending];

                this.params = [];
                const config = () => {
                    let res =
                        {
                            params: {
                                pageSize: pageSize,
                                pageNumber: pageNumber,
                                sortingColumn: sortingColumn,
                                sortingDirection: this.sortTable(sortingDirection),
                                query: this.search || ''
                            }
                        };
                    res.params.domainMask = this.viewData.domainMask;
                    return res;
                };

                this.getDataRequest(config);
            },

            getDataRequest(config) {
                this.loadingRequestCounter++;

                //пытаемся взять новые данные, пока не получим код 200
                this.$axios.get(this.$urls.DOMAIN_MASKS_DOMAINS, config())
                    .then(resp => {
                        if (resp.status !== 200) {
                            setTimeout(this.getDataRequest(config), 4000);
                        }
                        else {
                            /*
                            this.units.data = resp.data.content;
                            this.units.total = resp.data.totalElements;
                            this.units.pagination.totalItems = resp.data.totalElements;*/

                            let data = resp.data || [];
                            let p = config().params;

                            let start = p.pageNumber * p.pageSize;
                            let end = (p.pageNumber+1) * p.pageSize - 1;

                            start = start < data.length ? start : data.length;
                            end = end >= 0 ? end : 0;
                            end = end < data.length ? end : data.length;

                            this.units.data = data.filter((item, i) => i >= start && i <= end);
                            this.units.pagination.totalItems = data.length;
                            this.units.total = data.length;
                        }
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() =>{
                        this.loadingRequestCounter--;
                    })
            },

            updateUnit(unit) {
                if (this.modeCreate){
                    if (this.action === 'edit')
                        this.unitsNew.forEach((item, idx) => {
                            if (item.id === unit.id) {
                                this.$set(this.unitsNew, idx, unit);
                            }
                        });
                    else if (this.action === 'create') {
                        if (unit.id == null)
                            unit.id = "new" + ++this.unitCounter;
                        this.unitsNew.push(unit)
                    }
                }
                else if (this.modeView){
                    if (this.action === 'edit'){

                    }
                    else if (this.action === 'create') {
                        let params = {id: this.id};

                        this.$axios.post(this.$urls.DOMAIN_MASKS_ADD_DOMAIN, unit, {params})
                            .then(resp => {
                                this.showSuccessMsg({message: "Домен успешно добавлен"});
                                this.getData();
                            })
                            .catch(e => {
                                console.log('error', e);
                                this.showErrorMsg({message: "Ошибка при добавлении домена!"});
                            });
                    }
                }
            },

            create() {
                if (this.modeCreate){
                    let data = this.$merge({}, this.viewData);
                    delete data.id;
                    data.domains = this.unitsNew;
                    data.domains.forEach(function(v){ delete v.id });

                    this.$axios.post(this.$urls.DOMAIN_MASKS_ACTION, data)
                        .then(resp => {
                            this.initWithId(resp.data.id);
                            this.showSuccessMsg({message: "Данные успешно сохранены"});
                            //this.backToParent();
                        })
                        .catch(e => {
                            console.log('error', e);
                            this.showErrorMsg({message: "Ошибка при сохранении!"});
                        });
                }
            },

            onClickBack(){
                if (this.modeCreate && this.isValid) {
                    this.$confirm('Данные не сохранены, вы уверены что хотите уйти со страницы?',
                        {title: "Внимание!", buttonTrueText: "Да", buttonFalseText: "Нет"})
                        .then(res => {
                            if (res) {
                                this.backToParent();
                            }
                        })
                } else {
                    this.backToParent();
                }
            },

            backToParent() {
                this.$router.push({name: 'directorylocal', params: {dir_type: 'domain_masks'}});
            },

            sortTable(descending) {
                if (descending != null) {
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                } else return descending;
            },
        }
    }
</script>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
    .custom-text-field {
        width: 50%;
        font-size: 14px !important;
        padding-top: 0;
        margin-top: 0;
    }
</style>